/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.amqp_1_0.client;

import org.apache.qpid.amqp_1_0.messaging.SectionDecoder;
import org.apache.qpid.amqp_1_0.transport.DeliveryStateHandler;
import org.apache.qpid.amqp_1_0.transport.ReceivingLinkEndpoint;
import org.apache.qpid.amqp_1_0.transport.ReceivingLinkListener;

import org.apache.qpid.amqp_1_0.type.*;
import org.apache.qpid.amqp_1_0.type.DeliveryState;
import org.apache.qpid.amqp_1_0.type.messaging.*;
import org.apache.qpid.amqp_1_0.type.messaging.Source;
import org.apache.qpid.amqp_1_0.type.messaging.Target;
import org.apache.qpid.amqp_1_0.type.transaction.TransactionalState;
import org.apache.qpid.amqp_1_0.type.transport.ReceiverSettleMode;
import org.apache.qpid.amqp_1_0.type.transport.SenderSettleMode;
import org.apache.qpid.amqp_1_0.type.transport.Transfer;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Receiver implements DeliveryStateHandler
{
    private ReceivingLinkEndpoint _endpoint;
    private int _id;
    private static final UnsignedInteger DEFAULT_INITIAL_CREDIT = UnsignedInteger.valueOf(100);
    private Session _session;

    private Queue<Transfer> _prefetchQueue = new ConcurrentLinkedQueue<Transfer>();
    private Map<Binary, SettledAction> _unsettledMap = new HashMap<Binary, SettledAction>();



    public Receiver(final Session session, final String linkName, final String targetAddr, final String sourceAddr)
    {
        this(session, linkName, targetAddr, sourceAddr, null);
    }
    public Receiver(final Session session, final String linkName, final String targetAddr, final String sourceAddr, DistributionMode mode)
    {
        this(session, linkName, targetAddr, sourceAddr, mode, AcknowledgeMode.ALO);
    }


    public Receiver(final Session session,
                    final String linkName,
                    final String targetAddr,
                    final String sourceAddr,
                    final DistributionMode distMode,
                    final AcknowledgeMode ackMode)
    {
        this(session, linkName, createTarget(targetAddr), createSource(sourceAddr, distMode), ackMode);

    }

    public Receiver(final Session session,
                    final String linkName,
                    final String targetAddr,
                    final String sourceAddr,
                    final DistributionMode distMode,
                    final AcknowledgeMode ackMode,
                    boolean isDurable)
    {
        this(session, linkName, createTarget(targetAddr), createSource(sourceAddr, distMode), ackMode, isDurable);
    }


    public Receiver(final Session session,
                    final String linkName,
                    final String targetAddr,
                    final String sourceAddr,
                    final DistributionMode distMode,
                    final AcknowledgeMode ackMode,
                    boolean isDurable,
                    Map<Binary, Outcome> unsettled)
    {
        this(session, linkName, createTarget(targetAddr), createSource(sourceAddr, distMode), ackMode, isDurable,
             unsettled);
    }


    private static Source createSource(final String sourceAddr, final DistributionMode distMode)
    {
        Source source = new Source();
        source.setAddress(sourceAddr);
        source.setDistributionMode(distMode);
        return source;
    }

    private static Target createTarget(final String targetAddr)
    {
        Target target = new Target();
        target.setAddress(targetAddr);
        return target;
    }

    public Receiver(final Session session,
                    final String linkName,
                    final Target target,
                    final Source source,
                    final AcknowledgeMode ackMode)
    {
        this(session, linkName, target, source, ackMode, false);
    }
    public Receiver(final Session session,
                    final String linkName,
                    final Target target,
                    final Source source,
                    final AcknowledgeMode ackMode,
                    boolean isDurable)
    {
        this(session,linkName,target,source,ackMode,isDurable,null);
    }

    public Receiver(final Session session,
                    final String linkName,
                    final Target target,
                    final Source source,
                    final AcknowledgeMode ackMode,
                    final boolean isDurable,
                    final Map<Binary,Outcome> unsettled)
    {

        _session = session;
        if(isDurable)
        {
            source.setDurable(TerminusDurability.UNSETTLED_STATE);
            source.setExpiryPolicy(TerminusExpiryPolicy.NEVER);
        }
        _endpoint = session.getEndpoint().createReceivingLinkEndpoint(linkName, target, source,
                                                                      UnsignedInteger.ZERO);

        _endpoint.setDeliveryStateHandler(this);

        switch(ackMode)
        {
            case ALO:
                _endpoint.setSendingSettlementMode(SenderSettleMode.UNSETTLED);
                _endpoint.setReceivingSettlementMode(ReceiverSettleMode.FIRST);
                break;
            case AMO:
                _endpoint.setSendingSettlementMode(SenderSettleMode.SETTLED);
                _endpoint.setReceivingSettlementMode(ReceiverSettleMode.FIRST);
                break;
            case EO:
                _endpoint.setSendingSettlementMode(SenderSettleMode.UNSETTLED);
                _endpoint.setReceivingSettlementMode(ReceiverSettleMode.SECOND);
                break;

        }

        _endpoint.setLinkEventListener(new ReceivingLinkListener.DefaultLinkEventListener()
        {
            @Override public void messageTransfer(final Transfer xfr)
            {
                _prefetchQueue.add(xfr);
            }
        });

        _endpoint.setLocalUnsettled(unsettled);
        _endpoint.attach();


        synchronized(_endpoint.getLock())
        {
            while(!_endpoint.isAttached() || _endpoint.isDetached())
            {
                try
                {
                    _endpoint.getLock().wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public void setCredit(UnsignedInteger credit, boolean window)
    {
        _endpoint.setLinkCredit(credit);
        _endpoint.setCreditWindow(window);

    }


    public String getAddress()
    {
        return ((Source)_endpoint.getSource()).getAddress();
    }

    public Message receive()
    {
        return receive(-1L);
    }

    public Message receive(boolean wait)
    {
        return receive(wait ? -1L : 0L);
    }

    // 0 means no wait, -1 wait forever
    public Message receive(long wait)
    {
        Message m = null;
        Transfer xfr;
        long endTime = wait > 0L ? System.currentTimeMillis() + wait : 0L;

        while((xfr = receiveFromPrefetch(wait)) != null )
        {

            if(!Boolean.TRUE.equals(xfr.getAborted()))
            {
                Binary deliveryTag = xfr.getDeliveryTag();
                Boolean resume = xfr.getResume();

                List<Section> sections = new ArrayList<Section>();
                List<ByteBuffer> payloads = new ArrayList<ByteBuffer>();
                int totalSize = 0;

                boolean hasMore;
                do
                {
                    hasMore = Boolean.TRUE.equals(xfr.getMore());

                    ByteBuffer buf = xfr.getPayload();

                    if(buf != null)
                    {

                        totalSize += buf.remaining();

                        payloads.add(buf);
                    }
                    if(hasMore)
                    {
                        xfr = receiveFromPrefetch(0L);
                        if(xfr== null)
                        {
                            System.out.println("");
                        }
                    }
                }
                while(hasMore && !Boolean.TRUE.equals(xfr.getAborted()));

                if(!Boolean.TRUE.equals(xfr.getAborted()))
                {
                    ByteBuffer allPayload = ByteBuffer.allocate(totalSize);
                    for(ByteBuffer payload : payloads)
                    {
                        allPayload.put(payload);
                    }
                    allPayload.flip();
                    SectionDecoder decoder = _session.getSectionDecoder();

                    try
                    {
                        sections = decoder.parseAll(allPayload);
                    }
                    catch (AmqpErrorException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    m = new Message(sections);
                    m.setDeliveryTag(deliveryTag);
                    m.setResume(resume);
                    break;
                }
            }

            if(wait > 0L)
            {
                wait = endTime - System.currentTimeMillis();
                if(wait <=0L)
                {
                    break;
                }
            }
        }


        return m;

    }

    private Transfer receiveFromPrefetch(long wait)
        {
            long endTime = ((wait >0L) ? (System.currentTimeMillis() + wait) : 0L);
            final Object lock = _endpoint.getLock();
            synchronized(lock)
            {
                Transfer xfr;
                while(((xfr = _prefetchQueue.peek()) == null) && !_endpoint.isDrained())
                {
                    try
                    {
                        if(wait>=0L)
                        {
                            lock.wait(wait);
                        }
                        else
                        {
                            lock.wait();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    if(wait > 0L)
                    {
                        wait = endTime - System.currentTimeMillis();
                        if(wait <= 0L)
                        {
                            break;
                        }
                    }

                }
                if(xfr != null)
                {
                    _prefetchQueue.poll();

                }

                return xfr;
            }

        }


    public void acknowledge(final Message m)
    {
        acknowledge(m.getDeliveryTag());
    }

    public void acknowledge(final Message m, SettledAction a)
    {
        acknowledge(m.getDeliveryTag(), a);
    }


    public void acknowledge(final Message m, Transaction txn)
    {
        acknowledge(m.getDeliveryTag(), txn);
    }


    public void acknowledge(final Binary deliveryTag)
    {
        acknowledge(deliveryTag, null, null);
    }


    public void acknowledge(final Binary deliveryTag, SettledAction a)
    {
        acknowledge(deliveryTag, null, a);
    }

    public void acknowledge(final Binary deliveryTag, final Transaction txn)
    {
        acknowledge(deliveryTag, txn, null);
    }


    public void acknowledge(final Binary deliveryTag, final Transaction txn, SettledAction action)
    {

        Accepted accepted = new Accepted();
        DeliveryState state;
        if(txn != null)
        {
            TransactionalState txnState = new TransactionalState();
            txnState.setOutcome(accepted);
            txnState.setTxnId(txn.getTxnId());
            state = txnState;
        }
        else
        {
            state = accepted;
        }
        boolean settled = txn == null && !ReceiverSettleMode.SECOND.equals(_endpoint.getReceivingSettlementMode());

        if(!(settled || action == null))
        {
            _unsettledMap.put(deliveryTag, action);
        }

        _endpoint.updateDisposition(deliveryTag,state, settled);
    }

    public void acknowledgeAll(Message m)
    {
        acknowledgeAll(m.getDeliveryTag());
    }

    public void acknowledgeAll(Binary deliveryTag)
    {
        _endpoint.updateAllDisposition(deliveryTag, new Accepted(), true);
    }

    public void close()
    {
        _endpoint.setTarget(null);
        _endpoint.detach();
    }

    public void drain()
    {
        _endpoint.drain();
    }

    public void setCreditWithTransaction(final UnsignedInteger credit, final Transaction txn)
    {
        _endpoint.setLinkCredit(credit);
        _endpoint.setTransactionId(txn == null ? null : txn.getTxnId());
        _endpoint.setCreditWindow(false);

    }

    public void handle(final Binary deliveryTag, final DeliveryState state, final Boolean settled)
    {
        if(Boolean.TRUE.equals(settled))
        {
            SettledAction action = _unsettledMap.remove(deliveryTag);
            if(action != null)
            {
                action.onSettled(deliveryTag);
            }
        }
    }

    public Map<Binary, Outcome> getRemoteUnsettled()
    {
        return _endpoint.getInitialUnsettledMap();
    }


    public static interface SettledAction
    {
        public void onSettled(Binary deliveryTag);
    }
}