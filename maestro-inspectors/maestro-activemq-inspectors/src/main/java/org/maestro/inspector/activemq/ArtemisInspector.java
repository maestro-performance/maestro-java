package org.maestro.inspector.activemq;

import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.maestro.common.inspector.MaestroInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A inspector class for Apache ActiveMQ Artemis
 */
public class ArtemisInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisInspector.class);
    private boolean running = true;
    private String url;
    private String user;
    private String password;

    private J4pClient j4p;

    public ArtemisInspector() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void connect() {
        j4p = J4pClient.url(url)
                .user(user)
                .password(password)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .build();
    }

    public int start() throws Exception {
        running = true;
        connect();

        while (running) {

            J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory", "HeapMemoryUsage");

            J4pReadResponse response = j4p.execute(req);

            logger.debug("Heap Memory Usage: {}", (Object) response.getValue());
            Thread.sleep(1000);
        }

        logger.debug("Artemis inspector is terminating");
        return 0;
    }

    public void stop() throws Exception {
        running = false;
    }
}
