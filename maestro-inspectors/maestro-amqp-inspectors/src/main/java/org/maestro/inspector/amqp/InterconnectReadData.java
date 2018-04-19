package org.maestro.inspector.amqp;

import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsMapMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsMessageFacade;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.sound.midi.Sequence;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void sendRequest(String component) throws JMSException {
        Message message = createMessage(component);

        requestProducer.send(message);  //, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
    }

    public Message collectResponse() throws JMSException {
        return responseConsumer.receive(2000L);
    }

    public Message createMessage(String component) throws JMSException {

        System.out.println("org.apache.qpid.dispatch." + component);

        Message message = session.createMessage();
        message.setStringProperty("name", "self");
        message.setStringProperty("operation", "QUERY");
        message.setStringProperty("type", "org.amqp.management");
        Binary b = new Binary(("org.apache.qpid.dispatch." + component).getBytes());

//        AmqpJmsMapMessageFacade jmsMessageFacade = (AmqpJmsMapMessageFacade) ((JmsMessage) message).getFacade();

//        List<Object> emptyList = new ArrayList<Object>();


//        Method method = null;
//        try {
//            Class[] cArg = {Section.class};
//            method = jmsMessageFacade.getClass().getDeclaredMethod("setBody", cArg);
//            method.setAccessible(true);
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("attributeNames", new AmqpSequence(emptyList));
//            Section amqpValue = new AmqpValue(map);
//            method.invoke(jmsMessageFacade, amqpValue);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }

        ((JmsMessage) message).getFacade().setProperty("entityType", b);
//        ((ObjectMessage) message).setObject("attributeNames",  new AmqpSequence(emptyList));

        message.setJMSReplyTo(destination);
        message.setJMSCorrelationID("test");

        return message;
    }

    public Message collectRouterLink() {
        Message message = null;
        try {
            sendRequest("router.link");
            message = collectResponse();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return message;
    }
}
