package org.maestro.inspector.amqp;

import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.ConnectionsInfo;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.inspector.amqp.writers.ConnectionsInfoWriter;
import org.maestro.inspector.amqp.writers.RouteLinkInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * A class for Interconnect inspector based on AMQP management
 */
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

    /**
     * Start inspector
     * @return return code
     * @throws Exception implementation specific
     */
    @Override
    public int start() throws Exception {
        File logDir = TestLogUtils.nextTestLogDir(this.baseLogDir);
        InspectorProperties inspectorProperties = new InspectorProperties();

        RouteLinkInfoWriter routerLinkInfoWriter = new RouteLinkInfoWriter(logDir, "routerLink");
        ConnectionsInfoWriter connectionsInfoWriter = new ConnectionsInfoWriter(logDir, "connections");

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


            while (duration.canContinue(this) && isRunning()) {
                LocalDateTime now = LocalDateTime.now();


                routerLinkInfoWriter.write(now, readData.collectRouterLinkInfo());
                connectionsInfoWriter.write(now, readData.collectConnectionsInfo());

//                printOutput(readData.collectConnectionsInfo());

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
            connectionsInfoWriter.close();
        }
    }

//    @TODO Delete this, only support function
    @SuppressWarnings("unchecked")
    private void printOutput(ConnectionsInfo info) throws JMSException {

        List<Map<String, Object>> newList = info.getConnectionProperties();

        for (Map<String, Object> item: newList) {
            for (Map.Entry record : item.entrySet()) {
                System.out.println("Key: " + record.getKey());
                System.out.println("Value: " + record.getValue());
            }
        }
    }

    @Override
    public void stop() throws Exception {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
