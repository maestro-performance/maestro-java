package org.maestro.client.resolver;

import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.MaestroDeserializer;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.mqtt.MaestroMqttClient;
import org.maestro.client.exchange.mqtt.MqttConsumerEndpoint;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;

public final class MaestroClientResolver {

    private MaestroClientResolver() {}

    public static MaestroClient newClient(String maestroUrl) {
        MaestroMqttClient client = new MaestroMqttClient(maestroUrl);
        client.connect();

        return client;
    }

    public static ConsumerEndpoint<MaestroNote> newConsumerEndpoint(String maestroUrl, String[] topics) {
        MqttConsumerEndpoint<MaestroNote> consumerEndpoint = new MqttConsumerEndpoint<>(maestroUrl,
                MaestroDeserializer::deserialize);

        consumerEndpoint.connect();

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

        consumerEndpoint.connect();

        if (topics != null) {
            consumerEndpoint.subscribe(topics);
        }


        return consumerEndpoint;
    }

    public static ConsumerEndpoint<MaestroNote> newEventConsumerEndpoint(String maestroUrl) {
        return newEventConsumerEndpoint(maestroUrl, MaestroTopics.MAESTRO_TOPICS);
    }
}
