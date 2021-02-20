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

    public static MaestroClient newClient(String maestroUrl) {
        MaestroMqttClient client = new MaestroMqttClient(maestroUrl);
        client.connect();

        return client;
    }

    private static void doConnect(MqttConsumerEndpoint<MaestroNote> consumerEndpoint, String[] topics) {
        int connectionRetries = 10;
        int retryDelay = 1000;

        do {
            try {
                consumerEndpoint.connect();

                if (topics != null) {
                    consumerEndpoint.subscribe(topics);
                }
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

    public static ConsumerEndpoint<MaestroNote> newConsumerEndpoint(String maestroUrl, String[] topics) {
        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(maestroUrl,
                MaestroDeserializer::deserialize);

        doConnect(consumerEndpoint, topics);

        return consumerEndpoint;
    }

    public static ConsumerEndpoint<MaestroNote> newConsumerEndpoint(String maestroUrl) {
        return newConsumerEndpoint(maestroUrl, MaestroTopics.MAESTRO_TOPICS);
    }

    public static ConsumerEndpoint<MaestroNote> newEventConsumerEndpoint(String maestroUrl, String[] topics) {
        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(maestroUrl,
                MaestroDeserializer::deserializeEvent);

        doConnect(consumerEndpoint, topics);

        return consumerEndpoint;
    }

    public static ConsumerEndpoint<MaestroNote> newEventConsumerEndpoint(String maestroUrl) {
        return newEventConsumerEndpoint(maestroUrl, MaestroTopics.MAESTRO_TOPICS);
    }
}
