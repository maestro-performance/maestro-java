package org.maestro.inspector.activemq;

import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.OSInfo;
import org.maestro.common.inspector.types.RuntimeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private TestDuration duration;

    private ArtemisDataReader artemisDataReader;
    private J4pClient j4p;

    public ArtemisInspector() {

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDuration(final String duration) throws DurationParseException {
        this.duration = TestDurationBuilder.build(duration);
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

    public int start() throws Exception {
        try {
            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            connect();

            OSInfo osInfo = artemisDataReader.operatingSystem();
            logger.debug("Operating system: {}", osInfo);

            RuntimeInformation runtimeInformation = artemisDataReader.runtimeInformation();
            logger.debug("Runtime information: {}", runtimeInformation);

            while (duration.canContinue(this) && isRunning()) {
                logger.debug("Heap Memory Usage: {}", artemisDataReader.jvmHeapMemory());
                logger.debug("Eden Memory Usage: {}", artemisDataReader.jvmMemoryAreas());

                Thread.sleep(1000);
            }

            logger.debug("The test has finished and the Artemis inspector is terminating");
            return 0;
        } finally {
            startedEpochMillis = Long.MIN_VALUE;
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
