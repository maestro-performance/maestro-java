/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.inspector.amqp.writers.ConnectionsInfoWriter;
import org.maestro.inspector.amqp.writers.GeneralInfoWriter;
import org.maestro.inspector.amqp.writers.QDMemoryInfoWriter;
import org.maestro.inspector.amqp.writers.RouteLinkInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * A class for Interconnect inspector based on AMQP management
 */
public class InterconnectInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(InterconnectInspector.class);
    private long startedEpochMillis = Long.MIN_VALUE;
    private boolean running = false;
    private String url;
    @SuppressWarnings("FieldCanBeLocal")
    private String user;
    @SuppressWarnings("FieldCanBeLocal")
    private String password;
    private File baseLogDir;
    private TestDuration duration;
    private MaestroReceiver endpoint;

    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private MessageConsumer responseConsumer;
    private Destination tempDest;

    private WorkerOptions workerOptions;
    private InterconnectReadData interconnectReadData;

    private final int interval;

    public InterconnectInspector() {
        final AbstractConfiguration config = ConfigurationWrapper.getConfig();

        interval = config.getInteger("inspector.sleep.interval", 5000);
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
    public void setWorkerOptions(final WorkerOptions workerOptions) throws DurationParseException {
        this.duration = TestDurationBuilder.build(workerOptions.getDuration());

        this.workerOptions = workerOptions;
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
        ConnectionFactory connectionFactory = new org.apache.qpid.jms.JmsConnectionFactory(url);
        Destination queue = new org.apache.qpid.jms.JmsQueue("$management");

        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        messageProducer = session.createProducer(queue);

        tempDest = session.createTemporaryQueue();
        responseConsumer = session.createConsumer(tempDest);
    }

    private void checkCleanupErrorPolicy(final JMSException e) throws JMSException {
        final AbstractConfiguration config = ConfigurationWrapper.getConfig();
        final boolean cleanupErrorsIsFailure = config.getBoolean("inspector.cleanup.error.is.failure", false);

        if (cleanupErrorsIsFailure) {
            throw e;
        }
    }

    private void closeConnection() throws JMSException {
        try {
            if (session != null) {
                session.close();
            }
        }
        catch (JMSException e) {
            logger.warn("Error closing the JMS session: {}", e.getMessage(), e);
            checkCleanupErrorPolicy(e);
        }

        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (JMSException e) {
            logger.warn("Error closing the JMS connection: {}", e.getMessage(), e);
            checkCleanupErrorPolicy(e);
        }
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


        try (RouteLinkInfoWriter routerLinkInfoWriter = new RouteLinkInfoWriter(logDir, "routerLink");
             ConnectionsInfoWriter connectionsInfoWriter = new ConnectionsInfoWriter(logDir, "connections");
             QDMemoryInfoWriter qdMemoryInfoWriter = new QDMemoryInfoWriter(logDir, "qdmemory");
             GeneralInfoWriter generalInfoWriter = new GeneralInfoWriter(logDir, "general")
        )
        {
            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            logger.info("Inspector started");

            connect();

            interconnectReadData = new InterconnectReadData(session,
                    tempDest,
                    responseConsumer,
                    messageProducer);

            writeInspectorProperties(logDir, inspectorProperties, generalInfoWriter);


            while (duration.canContinue(this) && isRunning()) {
                LocalDateTime now = LocalDateTime.now();

                routerLinkInfoWriter.write(now, interconnectReadData.collectRouterLinkInfo());
                connectionsInfoWriter.write(now, interconnectReadData.collectConnectionsInfo());
                qdMemoryInfoWriter.write(now, interconnectReadData.collectMemoryInfo());
                generalInfoWriter.write(now, interconnectReadData.collectGeneralInfo());

                Thread.sleep(interval);
            }

            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            logger.debug("The test has finished and the Artemis inspector is terminating");

            return 0;
        } catch (InterruptedException eie) {
            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            return 0;
        } catch (Exception e) {
            TestLogUtils.createSymlinks(this.baseLogDir, true);
            endpoint.notifyFailure("Inspector failed");
            throw e;
        } finally {
            startedEpochMillis = Long.MIN_VALUE;
            closeConnection();
        }
    }

    private void writeInspectorProperties(File logDir, InspectorProperties inspectorProperties,
                                          GeneralInfoWriter generalInfoWriter) throws IOException, JMSException {
        setCommonProperties(inspectorProperties, workerOptions);

        generalInfoWriter.write(inspectorProperties, interconnectReadData.collectGeneralInfo());

        File propertiesFile = new File(logDir, InspectorProperties.FILENAME);
        inspectorProperties.write(propertiesFile);
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
