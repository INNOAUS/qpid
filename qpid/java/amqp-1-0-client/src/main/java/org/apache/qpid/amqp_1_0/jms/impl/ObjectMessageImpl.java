/*
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
 */

package org.apache.qpid.amqp_1_0.jms.impl;

import org.apache.qpid.amqp_1_0.jms.ObjectMessage;
import org.apache.qpid.amqp_1_0.type.Section;
import org.apache.qpid.amqp_1_0.type.messaging.*;
import org.apache.qpid.amqp_1_0.type.messaging.Properties;

import javax.jms.MessageNotWriteableException;
import java.io.Serializable;
import java.util.*;

public class ObjectMessageImpl extends MessageImpl implements ObjectMessage
{
    private Serializable _object;

    protected ObjectMessageImpl(Header header,
                                Properties properties,
                                Footer footer,
                                ApplicationProperties appProperties,
                                Serializable object,
                                SessionImpl session)
    {
        super(header, properties, appProperties, footer, session);
        _object = object;
    }

    protected ObjectMessageImpl(final SessionImpl session)
    {
        super(new Header(), new Properties(), new ApplicationProperties(new HashMap()), new Footer(Collections.EMPTY_MAP),
              session);
    }

    public void setObject(final Serializable serializable) throws MessageNotWriteableException
    {
        checkWritable();

        _object = serializable;
    }

    public Serializable getObject()
    {
        return _object;
    }

    @Override Collection<Section> getSections()
    {
        List<Section> sections = new ArrayList<Section>();
        sections.add(getHeader());
        sections.add(getProperties());
        sections.add(getApplicationProperties());
        AmqpValue section = new AmqpValue(_object);
        sections.add(section);
        sections.add(getFooter());
        return sections;
    }
}
