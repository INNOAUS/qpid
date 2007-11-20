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
package org.apache.qpid.server.protocol;

import junit.framework.TestCase;
import org.apache.mina.common.IoSession;
import org.apache.qpid.codec.AMQCodecFactory;
import org.apache.qpid.server.AMQChannel;
import org.apache.qpid.server.virtualhost.VirtualHost;
import org.apache.qpid.server.registry.IApplicationRegistry;
import org.apache.qpid.server.registry.ApplicationRegistry;
import org.apache.qpid.server.exchange.ExchangeRegistry;
import org.apache.qpid.server.queue.QueueRegistry;
import org.apache.qpid.server.queue.AMQQueue;
import org.apache.qpid.server.store.MessageStore;
import org.apache.qpid.server.store.SkeletonMessageStore;
import org.apache.qpid.AMQException;
import org.apache.qpid.protocol.AMQConstant;
import org.apache.qpid.framing.AMQShortString;

import javax.management.JMException;

/** Test class to test MBean operations for AMQMinaProtocolSession. */
public class MaxChannelsTest extends TestCase
{
//    private MessageStore _messageStore = new SkeletonMessageStore();

    public void testChannels() throws Exception
    {
        IApplicationRegistry appRegistry = ApplicationRegistry.getInstance();
        AMQMinaProtocolSession _protocolSession = new AMQMinaProtocolSession(new MockIoSession(),
                                                                             appRegistry.getVirtualHostRegistry(),
                                                                             new AMQCodecFactory(true),
                                                                             null);
        _protocolSession.setVirtualHost(appRegistry.getVirtualHostRegistry().getVirtualHost("test"));

        // check the channel count is correct
        int channelCount = _protocolSession.getChannels().size();
        assertEquals("Initial channel count wrong", 0, channelCount);

        long maxChannels = 10L;
        _protocolSession.setMaximumNumberOfChannels(maxChannels);
        assertEquals("Number of channels not correctly set.", new Long(maxChannels), _protocolSession.getMaximumNumberOfChannels());


        try
        {
            for (long currentChannel = 0L; currentChannel < maxChannels; currentChannel++)
            {
                _protocolSession.addChannel(new AMQChannel(_protocolSession, (int) currentChannel, null, null));
            }
        }
        catch (AMQException e)
        {
            assertEquals("Wrong exception recevied.", e.getErrorCode(), AMQConstant.NOT_ALLOWED);
        }
        assertEquals("Maximum number of channels not set.", new Long(maxChannels), new Long(_protocolSession.getChannels().size()));
    }

}
