package org.maestro.inspector.amqp;

import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.proton.amqp.Binary;
import org.maestro.common.inspector.types.RouterLinkInfo;
import org.maestro.inspector.amqp.converter.InterconnectInfoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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

    private Message collectData(String component) throws JMSException {
        requestProducer.send(createMessage(component));

        return collectResponse();
    }

    private Message collectResponse() throws JMSException {
        return responseConsumer.receive(2000L);
    }

    private Message createMessage(String component) throws JMSException {

        System.out.println("org.apache.qpid.dispatch." + component);

        Message message = session.createObjectMessage();
        message.setBooleanProperty("JMS_AMQP_TYPED_ENCODING", true);

        message.setStringProperty("name", "self");
        message.setStringProperty("operation", "QUERY");
        message.setStringProperty("type", "org.amqp.management");
        Binary b = new Binary(("org.apache.qpid.dispatch." + component).getBytes());

        ((JmsMessage) message).getFacade().setProperty("entityType", b);

        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("attributeNames", new ArrayList<>());
        ((ObjectMessage) message).setObject(map);

        message.setJMSReplyTo(destination);
        message.setJMSCorrelationID("test");

        return message;
    }

    @SuppressWarnings("unchecked")
    RouterLinkInfo collectRouterLinkInfo() throws JMSException {

        InterconnectInfoConverter convertor = new InterconnectInfoConverter();

        Map receivedMessage = collectData("router.link").getBody(HashMap.class);

        return new RouterLinkInfo(convertor.parseReceivedMessage(receivedMessage));
    }
}
