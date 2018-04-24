package org.maestro.inspector.amqp;

import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.RouterLinkInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.inspector.amqp.converter.RouterLinkInfoConverter;
import org.maestro.inspector.amqp.writers.RouteLinkInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.File;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.*;

public class InterconnectInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(InterconnectInspector.class);
    private long startedEpochMillis = Long.MIN_VALUE;
    private boolean running = false;
    private String url;
    private String user;
    private String password;
    private File baseLogDir;
    private TestDuration duration;
    private MaestroReceiver endpoint;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private MessageConsumer responseConsumer;
    private Destination tempDest;

    public InterconnectInspector() {

    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setDuration(String duration) throws DurationParseException {
        this.duration = TestDurationBuilder.build(duration);
    }

    @Override
    public void setBaseLogDir(File baseLogDir) {
        this.baseLogDir = baseLogDir;
    }

    @Override
    public void setEndpoint(MaestroReceiver endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isRunning() {
        return running;
    }

    private void connect() throws JMSException {
        connectionFactory = new org.apache.qpid.jms.JmsConnectionFactory(url);
        Destination queue = new org.apache.qpid.jms.JmsQueue("$management");

        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        messageProducer = session.createProducer(queue);

        tempDest = session.createTemporaryQueue();
        responseConsumer = session.createConsumer(tempDest);
    }

    private void closeConnection() throws JMSException {
        session.close();
        connection.close();
    }

    @Override
    public int start() throws Exception {
        File logDir = TestLogUtils.nextTestLogDir(this.baseLogDir);
        InspectorProperties inspectorProperties = new InspectorProperties();

        RouterLinkInfoConverter convertor = new RouterLinkInfoConverter();
        RouteLinkInfoWriter routerLinkInfoWriter = new RouteLinkInfoWriter(logDir, "routerLink");

        try {
            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            logger.info("Inspector started");

            connect();

            InterconnectReadData readData = new InterconnectReadData(session,
                    tempDest,
                    responseConsumer,
                    messageProducer);

            Message receivedMessage;


            while (duration.canContinue(this) && isRunning()) {
                LocalDateTime now = LocalDateTime.now();

                receivedMessage = readData.collectData("router.link");


                if (receivedMessage != null) {
                    System.out.println(receivedMessage.getStringProperty("statusCode"));

                    Map receivedMap = receivedMessage.getBody(LinkedHashMap.class);

                    RouterLinkInfo routerLinkInfo = convertor.parseReceivedMessage(receivedMap);

                    routerLinkInfoWriter.write(now, routerLinkInfo);

                }

                Thread.sleep(5000);
            }

            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            logger.debug("The test has finished and the Artemis inspector is terminating");

            return 0;
        } catch (InterruptedException eie) {
            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            throw eie;
        } catch (Exception e) {
            TestLogUtils.createSymlinks(this.baseLogDir, true);
            endpoint.notifyFailure("Inspector failed");
            throw e;
        } finally {
            startedEpochMillis = Long.MIN_VALUE;
            closeConnection();
            routerLinkInfoWriter.close();
        }
    }

//    private void printOutput(Message message) throws JMSException {
//        Map receivedMap = message.getBody(LinkedHashMap.class);
//        RouterLinkInfoConverter convertor = new RouterLinkInfoConverter();
//
//        ArrayList<Map<Object, Object>> recordList = convertor.parseReceivedMessage(receivedMap);
//
//        for (Map<Object, Object> item: recordList) {
//            for (Map.Entry record : item.entrySet()) {
//                System.out.println("Key: " + record.getKey());
//                System.out.println("Value: " + record.getValue());
//            }
//        }
//    }

    @Override
    public void stop() throws Exception {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
