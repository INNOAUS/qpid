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
package org.apache.qpid.server.virtualhost;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.apache.qpid.server.configuration.VirtualHostConfiguration;
import org.apache.qpid.server.configuration.updater.TaskExecutor;
import org.apache.qpid.server.connection.IConnectionRegistry;
import org.apache.qpid.server.exchange.ExchangeImpl;
import org.apache.qpid.server.logging.EventLogger;
import org.apache.qpid.server.message.MessageDestination;
import org.apache.qpid.server.message.MessageSource;
import org.apache.qpid.server.plugin.ExchangeType;
import org.apache.qpid.server.protocol.LinkRegistry;
import org.apache.qpid.server.queue.AMQQueue;
import org.apache.qpid.server.queue.QueueRegistry;
import org.apache.qpid.server.security.SecurityManager;
import org.apache.qpid.server.security.auth.manager.AuthenticationManager;
import org.apache.qpid.server.stats.StatisticsCounter;
import org.apache.qpid.server.store.DurableConfigurationStore;
import org.apache.qpid.server.store.MessageStore;
import org.apache.qpid.server.txn.DtxRegistry;

import java.util.UUID;

public class MockVirtualHost implements VirtualHost
{
    private String _name;

    public MockVirtualHost(String name)
    {
        _name = name;
    }

    public void close()
    {

    }

    @Override
    public VirtualHostRegistry getVirtualHostRegistry()
    {
        return null;
    }

    public AuthenticationManager getAuthenticationManager()
    {
        return null;
    }

    public DtxRegistry getDtxRegistry()
    {
        return null;
    }

    public VirtualHostConfiguration getConfiguration()
    {
        return null;
    }

    public IConnectionRegistry getConnectionRegistry()
    {
        return null;
    }

    public int getHouseKeepingActiveCount()
    {
        return 0;
    }

    public long getHouseKeepingCompletedTaskCount()
    {
        return 0;
    }

    public int getHouseKeepingPoolSize()
    {
        return 0;
    }

    public long getHouseKeepingTaskCount()
    {
        return 0;
    }

    public MessageStore getMessageStore()
    {
        return null;
    }

    public DurableConfigurationStore getDurableConfigurationStore()
    {
        return null;
    }

    public String getName()
    {
        return _name;
    }

    public QueueRegistry getQueueRegistry()
    {
        return null;
    }

    @Override
    public AMQQueue getQueue(String name)
    {
        return null;
    }

    @Override
    public MessageSource getMessageSource(final String name)
    {
        return null;
    }

    @Override
    public AMQQueue getQueue(UUID id)
    {
        return null;
    }

    @Override
    public Collection<AMQQueue> getQueues()
    {
        return null;
    }

    @Override
    public int removeQueue(AMQQueue queue)
    {
        return 0;
    }

    @Override
    public AMQQueue createQueue(Map<String, Object> arguments)
    {
        return null;
    }

    @Override
    public ExchangeImpl createExchange(Map<String,Object> attributes)
    {
        return null;
    }

    @Override
    public void removeExchange(ExchangeImpl exchange, boolean force)
    {
    }

    @Override
    public MessageDestination getMessageDestination(final String name)
    {
        return null;
    }

    @Override
    public ExchangeImpl getExchange(String name)
    {
        return null;
    }

    @Override
    public ExchangeImpl getExchange(UUID id)
    {
        return null;
    }

    @Override
    public ExchangeImpl getDefaultDestination()
    {
        return null;
    }

    @Override
    public Collection<ExchangeImpl<?>> getExchanges()
    {
        return null;
    }

    @Override
    public Collection<ExchangeType<? extends ExchangeImpl>> getExchangeTypes()
    {
        return null;
    }

    public SecurityManager getSecurityManager()
    {
        return null;
    }

    @Override
    public void addVirtualHostListener(VirtualHostListener listener)
    {
    }

    public LinkRegistry getLinkRegistry(String remoteContainerId)
    {
        return null;
    }

    public ScheduledFuture<?> scheduleTask(long delay, Runnable timeoutTask)
    {
        return null;
    }

    public void scheduleHouseKeepingTask(long period, HouseKeepingTask task)
    {

    }

    public void setHouseKeepingPoolSize(int newSize)
    {

    }


    public long getCreateTime()
    {
        return 0;
    }

    public UUID getId()
    {
        return null;
    }

    public boolean isDurable()
    {
        return false;
    }

    public StatisticsCounter getDataDeliveryStatistics()
    {
        return null;
    }

    public StatisticsCounter getDataReceiptStatistics()
    {
        return null;
    }

    public StatisticsCounter getMessageDeliveryStatistics()
    {
        return null;
    }

    public StatisticsCounter getMessageReceiptStatistics()
    {
        return null;
    }

    public void initialiseStatistics()
    {

    }

    public void registerMessageDelivered(long messageSize)
    {

    }

    public void registerMessageReceived(long messageSize, long timestamp)
    {

    }

    public void resetStatistics()
    {

    }

    public State getState()
    {
        return State.ACTIVE;
    }

    public void block()
    {
    }

    public void unblock()
    {
    }

    @Override
    public long getDefaultAlertThresholdMessageAge()
    {
        return 0;
    }

    @Override
    public long getDefaultAlertThresholdMessageSize()
    {
        return 0;
    }

    @Override
    public long getDefaultAlertThresholdQueueDepthMessages()
    {
        return 0;
    }

    @Override
    public long getDefaultAlertThresholdQueueDepthBytes()
    {
        return 0;
    }

    @Override
    public long getDefaultAlertRepeatGap()
    {
        return 0;
    }

    @Override
    public long getDefaultQueueFlowControlSizeBytes()
    {
        return 0;
    }

    @Override
    public long getDefaultQueueFlowResumeSizeBytes()
    {
        return 0;
    }

    @Override
    public int getDefaultMaximumDeliveryAttempts()
    {
        return 0;
    }

    @Override
    public TaskExecutor getTaskExecutor()
    {
        return null;
    }

    @Override
    public org.apache.qpid.server.model.VirtualHost getModel()
    {
        return null;
    }

    @Override
    public EventLogger getEventLogger()
    {
        return null;
    }
}