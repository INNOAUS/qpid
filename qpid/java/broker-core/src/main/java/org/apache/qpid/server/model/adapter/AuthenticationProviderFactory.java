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
package org.apache.qpid.server.model.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.qpid.server.model.AuthenticationProvider;
import org.apache.qpid.server.model.Broker;
import org.apache.qpid.server.plugin.AuthenticationManagerFactory;
import org.apache.qpid.server.plugin.QpidServiceLoader;
import org.apache.qpid.server.security.auth.manager.AbstractAuthenticationManager;

public class AuthenticationProviderFactory
{
    private final Iterable<AuthenticationManagerFactory> _factories;
    private Collection<String> _supportedAuthenticationProviders;

    public AuthenticationProviderFactory(QpidServiceLoader<AuthenticationManagerFactory> authManagerFactoryServiceLoader)
    {
        _factories = authManagerFactoryServiceLoader.atLeastOneInstanceOf(AuthenticationManagerFactory.class);
        List<String> supportedAuthenticationProviders = new ArrayList<String>();
        for (AuthenticationManagerFactory factory : _factories)
        {
            supportedAuthenticationProviders.add(factory.getType());
        }
        _supportedAuthenticationProviders = Collections.unmodifiableCollection(supportedAuthenticationProviders);
    }

    /**
     * Creates {@link AuthenticationProvider} for given ID, {@link Broker} and attributes.
     * <p>
     * The configured {@link AuthenticationManagerFactory}'s are used to try to create the {@link AuthenticationProvider}.
     * The first non-null instance is returned. The factories are used in non-deterministic order.
     */
    public AuthenticationProvider create(UUID id, Broker broker, Map<String, Object> attributes)
    {
        return createAuthenticationProvider(id, broker, attributes, false);
    }

    /**
     * Recovers {@link AuthenticationProvider} for given ID, attributes and {@link Broker}.
     * <p>
     * The configured {@link AuthenticationManagerFactory}'s are used to try to create the {@link AuthenticationProvider}.
     * The first non-null instance is returned. The factories are used in non-deterministic order.
     */
    public AuthenticationProvider recover(UUID id, Map<String, Object> attributes, Broker broker)
    {
        return createAuthenticationProvider(id, broker, attributes, true);
    }

    private AuthenticationProvider createAuthenticationProvider(UUID id, Broker broker, Map<String, Object> attributes, boolean recovering)
    {
        attributes = new HashMap<String, Object>(attributes);
        attributes.put(AuthenticationProvider.ID,id);

        for (AuthenticationManagerFactory factory : _factories)
        {
            AbstractAuthenticationManager manager = factory.createInstance(broker, attributes, recovering);
            if (manager != null)
            {
                return manager;
            }
        }

        throw new IllegalArgumentException("No authentication provider factory found for configuration attributes " + attributes);
    }

    public Collection<String> getSupportedAuthenticationProviders()
    {
        return _supportedAuthenticationProviders;
    }
}