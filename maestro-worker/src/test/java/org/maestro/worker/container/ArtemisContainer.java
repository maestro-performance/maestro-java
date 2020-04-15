package org.maestro.worker.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class ArtemisContainer extends GenericContainer {
    public static final int DEFAULT_MQTT_PORT = 1883;
    public static final int DEFAULT_AMQP_PORT = 5672;
    public static final int DEFAULT_ADMIN_PORT = 8161;
    public static final int DEFAULT_ACCEPTOR_PORT = 61616;


    public ArtemisContainer() {
        super(new ImageFromDockerfile("apache-artemis:2.6", false)
                .withFileFromClasspath("Dockerfile",
                        "org/maestro/worker/container/artemis/Dockerfile"));

        withExposedPorts(DEFAULT_MQTT_PORT, DEFAULT_AMQP_PORT,
                DEFAULT_ADMIN_PORT, DEFAULT_ACCEPTOR_PORT);

        waitingFor(Wait.forListeningPort());
    }

    public ArtemisContainer(Integer... ports) {
        super(new ImageFromDockerfile("apache-artemis:2.6", false)
                .withFileFromClasspath("Dockerfile",
                        "org/maestro/worker/container/artemis/Dockerfile"));

        withExposedPorts(ports);

        waitingFor(Wait.forListeningPort());
    }


    /**
     * Gets the port number used for exchanging messages using the AMQP protocol
     * @return the port number
     */
    public int getAMQPPort() {
        return getMappedPort(DEFAULT_AMQP_PORT);
    }


    /**
     * Gets the end point URL used exchanging messages using the AMQP protocol (ie.: tcp://host:${amqp.port})
     * @return the end point URL as a string
     */
    public String getAMQPEndpoint() {
        return String.format("amqp://localhost:%d", getAMQPPort());
    }


    /**
     * Gets the port number used for exchanging messages using the MQTT protocol
     * @return the port number
     */
    public int getMQTTPort() {
        return getMappedPort(DEFAULT_MQTT_PORT);
    }


    /**
     * Gets the end point URL used exchanging messages using the MQTT protocol (ie.: tcp://host:${mqtt.port})
     * @return the end point URL as a string
     */
    public String getMQTTEndpoint() {
        return String.format("tcp://localhost:%d", getMQTTPort());
    }


    /**
     * Gets the port number used for accessing the web management console or the management API
     * @return the port number
     */
    public int getAdminPort() {
        return getMappedPort(DEFAULT_ADMIN_PORT);
    }


    /**
     * Gets the end point URL used for accessing the web management console or the management API
     * @return the admin URL as a string
     */
    public String getAdminURL() {
        return String.format("http://localhost:%d", getAdminPort());
    }


    /**
     * Gets the port number used for exchanging messages using the default acceptor port
     * @return the port number
     */
    public int getDefaultAcceptorPort() {
        return getMappedPort(DEFAULT_ACCEPTOR_PORT);
    }


    /**
     * Gets the end point URL used exchanging messages through the default acceptor port
     * @return the end point URL as a string
     */
    public String getDefaultEndpoint() {
        return String.format("tcp://localhost:%d", getDefaultAcceptorPort());
    }


    /**
     * Gets the port number used for exchanging messages using the Openwire protocol
     * @return the port number
     */
    public int getOpenwirePort() {
        return getDefaultAcceptorPort();
    }


    /**
     * Gets the end point URL used exchanging messages using the Openwire protocol (ie.: tcp://host:${amqp.port})
     * @return the end point URL as a string
     */
    public String getOpenwireEndpoint() {
        return String.format("tcp://localhost:%d", getOpenwirePort());
    }
}
