package org.maestro.client.resolver;

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.mqtt.MaestroMqttClient;
import org.maestro.client.exchange.mqtt.MqttConsumerEndpoint;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MaestroClientResolver {
    private static final Logger logger = LoggerFactory.getLogger(MaestroClientResolver.class);

    private MaestroClientResolver() {}

    interface Connector {
        void connect();
    }

    private static void doConnect(Connector connector) {
        int connectionRetries = 10;
        int retryDelay = 1000;

        do {
            try {
                logger.info("Trying to connect to broker: {} retries remaining", connectionRetries);
                connector.connect();
                logger.info("Successfully connected to the Maestro broker");
                break;
            } catch (MaestroConnectionException e) {
                logger.error("Maestro did not connect. Waiting and retrying {} more times", connectionRetries);
                connectionRetries--;

                if (connectionRetries > 0) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e1) {
                        break;
                    }
                }
                else {
                    System.err.println(e.getMessage());
                }
            }
        } while (connectionRetries > 0);
    }

    public static MaestroClient newClient(String maestroUrl) {
        MaestroMqttClient client = new MaestroMqttClient(maestroUrl);

        doConnect(() -> client.connect());

        return client;
    }

    public static ConsumerEndpoint<MaestroNote> newConsumerEndpoint(String maestroUrl, String[] topics) {
        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(maestroUrl,
                MaestroDeserializer::deserialize);

        doConnect(() -> consumerEndpoint.connect());

        if (topics != null) {
            consumerEndpoint.subscribe(topics);
        }

        return consumerEndpoint;
    }

    public static ConsumerEndpoint<MaestroNote> newConsumerEndpoint(String maestroUrl) {
        return newConsumerEndpoint(maestroUrl, MaestroTopics.MAESTRO_TOPICS);
    }

    public static ConsumerEndpoint<MaestroNote> newEventConsumerEndpoint(String maestroUrl, String[] topics) {
        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(maestroUrl,
                MaestroDeserializer::deserializeEvent);

        doConnect(() -> consumerEndpoint.connect());

        if (topics != null) {
            consumerEndpoint.subscribe(topics);
        }

        return consumerEndpoint;
    }

    public static ConsumerEndpoint<MaestroNote> newEventConsumerEndpoint(String maestroUrl) {
        return newEventConsumerEndpoint(maestroUrl, MaestroTopics.MAESTRO_TOPICS);
    }
}
