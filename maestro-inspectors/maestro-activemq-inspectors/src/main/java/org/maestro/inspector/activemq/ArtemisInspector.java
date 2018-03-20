package org.maestro.inspector.activemq;

import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.*;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.inspector.activemq.writers.JVMMemoryInfoWriter;
import org.maestro.inspector.activemq.writers.OSInfoWriter;
import org.maestro.inspector.activemq.writers.QueueInfoWriter;
import org.maestro.inspector.activemq.writers.RuntimeInfoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A inspector class for Apache ActiveMQ Artemis
 */
public class ArtemisInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisInspector.class);
    private long startedEpochMillis = Long.MIN_VALUE;
    private boolean running = false;
    private String url;
    private String user;
    private String password;
    private File baseLogDir;
    private TestDuration duration;

    private ArtemisDataReader artemisDataReader;
    private J4pClient j4p;



    public ArtemisInspector() {

    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setDuration(final String duration) throws DurationParseException {
        this.duration = TestDurationBuilder.build(duration);
    }

    @Override
    public void setBaseLogDir(final File baseLogDir) {
        this.baseLogDir = baseLogDir;
    }

    public boolean isRunning() {
        return running;
    }

    private void connect() {
        j4p = J4pClient.url(url)
                .user(user)
                .password(password)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .build();

        artemisDataReader = new ArtemisDataReader(j4p);
    }

    /*
        prop.setProperty("productName", productName);
        prop.setProperty("productVersion", productVersion);
     */
    public int start() throws Exception {
        File logDir = TestLogUtils.nextTestLogDir(this.baseLogDir);
        InspectorProperties inspectorProperties = new InspectorProperties();

        JVMMemoryInfoWriter heapMemoryWriter = new JVMMemoryInfoWriter(logDir, "heap");
        JVMMemoryInfoWriter jvmMemoryAreasWriter = new JVMMemoryInfoWriter(logDir, "memory-areas");
        RuntimeInfoWriter runtimeInfoWriter = new RuntimeInfoWriter(inspectorProperties);
        OSInfoWriter osInfoWriter = new OSInfoWriter(inspectorProperties);
        QueueInfoWriter queueInfoWriter = new QueueInfoWriter(logDir, "queues");

        try {
//            JVMMemoryInfoWriter heapMemoryWriter = new JVMMemoryInfoWriter(logDir, "heap");
//            JVMMemoryInfoWriter jvmMemoryAreasWriter = new JVMMemoryInfoWriter(logDir, "memory-areas");
//            RuntimeInfoWriter runtimeInfoWriter = new RuntimeInfoWriter(inspectorProperties);
//            OSInfoWriter osInfoWriter = new OSInfoWriter(inspectorProperties);
//            QueueInfoWriter queueInfoWriter = new QueueInfoWriter(logDir, "queues");

            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            connect();

            OSInfo osInfo = artemisDataReader.operatingSystem();
            osInfoWriter.write(osInfo);

            RuntimeInfo runtimeInfo = artemisDataReader.runtimeInformation();
            runtimeInfoWriter.write(runtimeInfo);

            File propertiesFile = new File(logDir, "inspector.properties");
            inspectorProperties.write(propertiesFile);

            while (duration.canContinue(this) && isRunning()) {
                LocalDateTime now = LocalDateTime.now();
                heapMemoryWriter.write(artemisDataReader.jvmHeapMemory());

                List<JVMMemoryInfo> memoryInfoList = artemisDataReader.jvmMemoryAreas();
                for (JVMMemoryInfo memoryInfo : memoryInfoList) {
                    jvmMemoryAreasWriter.write(memoryInfo);
                }

                try {
                    QueueInfo queueInfoList = artemisDataReader.queueInformation();
                    queueInfoWriter.write(queueInfoList);
                }
                catch (Exception e) {
                    logger.error("Unable to read queue information: {}", e.getMessage(), e);
                }

                Thread.sleep(5000);
            }

            logger.debug("The test has finished and the Artemis inspector is terminating");
            return 0;
        } finally {
            startedEpochMillis = Long.MIN_VALUE;

            heapMemoryWriter.close();
            jvmMemoryAreasWriter.close();
            queueInfoWriter.close();
        }
    }

    public void stop() throws Exception {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
