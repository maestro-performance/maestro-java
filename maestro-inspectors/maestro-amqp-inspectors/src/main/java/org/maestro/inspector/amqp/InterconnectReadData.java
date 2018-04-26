package org.maestro.inspector.amqp;

import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.proton.amqp.Binary;
import org.maestro.common.inspector.types.ConnectionsInfo;
import org.maestro.common.inspector.types.QDMemoryInfo;
import org.maestro.common.inspector.types.RouterLinkInfo;
import org.maestro.inspector.amqp.converter.InterconnectInfoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Class for read data about Interconnect
 */
public class InterconnectReadData {
    private static final Logger logger = LoggerFactory.getLogger(InterconnectReadData.class);
    private Session session;
    private Destination destination;
    private MessageConsumer responseConsumer;
    private MessageProducer requestProducer;

    public InterconnectReadData(Session session,
                                Destination destination,
                                MessageConsumer responseConsumer,
                                MessageProducer requestProducer) {
        this.session = session;
        this.destination = destination;
        this.requestProducer = requestProducer;
        this.responseConsumer = responseConsumer;
    }

    /**
     * Collect data from Interconnect
     * @param component string representation component name
     * @return response collector
     * @throws JMSException if it can't send or receive message
     */
    private Message collectData(String component) throws JMSException {
        requestProducer.send(createMessage(component));

        return collectResponse();
    }

    /**
     * Collect response from Interconnect
     * @return received message
     * @throws JMSException if it can't receive response
     */
    private Message collectResponse() throws JMSException {
        return responseConsumer.receive(2000L);
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

        Map receivedMessage = collectData("router.link").getBody(HashMap.class);

        return new RouterLinkInfo(converter.parseReceivedMessage(receivedMessage));
    }

    /**
     * Collect information about Connections.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    ConnectionsInfo collectConnectionsInfo() throws JMSException {

        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        Map receivedMessage = collectData("connection").getBody(HashMap.class);

        return new ConnectionsInfo(converter.parseReceivedMessage(receivedMessage));
    }

    /**
     * Collect information about used Memory.
     * @return parsed response
     * @throws JMSException if it can't collect proper information
     */
    @SuppressWarnings("unchecked")
    QDMemoryInfo collectMemoryInfo() throws JMSException {
        InterconnectInfoConverter converter = new InterconnectInfoConverter();

        Map receivedMessage = collectData("allocator").getBody(HashMap.class);

        return new QDMemoryInfo(converter.parseReceivedMessage(receivedMessage));
    }
}
