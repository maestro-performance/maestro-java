/*
 * Copyright 2018 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.inspector.amqp;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.proton.amqp.Binary;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.inspector.types.ConnectionsInfo;
import org.maestro.common.inspector.types.GeneralInfo;
import org.maestro.common.inspector.types.QDMemoryInfo;
import org.maestro.common.inspector.types.RouterLinkInfo;
import org.maestro.inspector.amqp.converter.InterconnectInfoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class for read data about Interconnect
 */
public class InterconnectReadData {
    private static final Logger logger = LoggerFactory.getLogger(InterconnectReadData.class);
    private final Session session;
    private final Destination destination;
    private final MessageConsumer responseConsumer;
    private final MessageProducer requestProducer;
    private final long timeout;

    public InterconnectReadData(Session session,
                                Destination destination,
                                MessageConsumer responseConsumer,
                                MessageProducer requestProducer) {
        this.session = session;
        this.destination = destination;
        this.requestProducer = requestProducer;
        this.responseConsumer = responseConsumer;

        AbstractConfiguration config = ConfigurationWrapper.getConfig();
        timeout = config.getInteger("inspector.amqp.management.timeout", 2000);
    }

    /**
     * Collect data from Interconnect
     * @param component string representation component name
     * @return A hash map with the collected data as read from the message. If
     * unable to read the message, returns an empty map
     * @throws JMSException if it can't send or receive message
     */
    private HashMap<?, ?> collectData(final String component) throws JMSException {
        requestProducer.send(createMessage(component));

        Message message = collectResponse();
        if (message == null) {
            logger.warn("No message was received, returning an empty data map");
            return new HashMap<>();
        }

        return message.getBody(HashMap.class);

    }

    /**
     * Collect response from Interconnect
     * @return received message
     * @throws JMSException if it can't receive response
     */
    private Message collectResponse() throws JMSException {
        return responseConsumer.receive(timeout);
    }

    /**
     * Create request message
     * @param component string representation component name
     * @return created message
     * @throws JMSException if unable to create message
     */
    private Message createMessage(String component) throws JMSException {
        logger.debug("Creating request message for: {}", component);

        Message message = session.createObjectMessage();
        message.setBooleanProperty("JMS_AMQP_TYPED_ENCODING", true);

        message.setStringProperty("name", "self");
        message.setStringProperty("operation", "QUERY");
        message.setStringProperty("type", "org.amqp.management");
        Binary requestFor = new Binary(("org.apache.qpid.dispatch." + component).getBytes());

        ((JmsMessage) message).getFacade().setProperty("entityType", requestFor);

        HashMap<String,Object> map = new HashMap<>();
        map.put("attributeNames", new ArrayList<>());
        ((ObjectMessage) message).setObject(map);

        message.setJMSReplyTo(destination);

        return message;
    }


    /**
     * Collect information about Router Links.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    RouterLinkInfo collectRouterLinkInfo() throws JMSException {

        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        HashMap<?, ?> collectedData = collectData("router.link");

        return new RouterLinkInfo(converter.parseReceivedMessage(collectedData));
    }

    /**
     * Collect information about Connections.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    ConnectionsInfo collectConnectionsInfo() throws JMSException {

        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        HashMap<?, ?> collectedData = collectData("connection");

        return new ConnectionsInfo(converter.parseReceivedMessage(collectedData));
    }

    /**
     * Collect information about used Memory.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    QDMemoryInfo collectMemoryInfo() throws JMSException {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        HashMap<?, ?> collectedData = collectData("allocator");

        return new QDMemoryInfo(converter.parseReceivedMessage(collectedData));
    }

    /**
     * Collect information about general information.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    GeneralInfo collectGeneralInfo() throws JMSException {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        HashMap<?, ?> collectedData = collectData("router");

        return new GeneralInfo(converter.parseReceivedMessage(collectedData));
    }
}
