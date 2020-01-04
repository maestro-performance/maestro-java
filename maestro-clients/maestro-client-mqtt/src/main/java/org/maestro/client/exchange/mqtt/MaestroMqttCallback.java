package org.maestro.client.exchange.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MaestroMqttCallback implements MqttCallbackExtended {
    private static final Logger logger = LoggerFactory.getLogger(MaestroMqttCallback.class);
    private static final Set<Subscription> subscriptions = new LinkedHashSet<>();
    private final Consumer<Boolean> resubscribe;
    private final BiConsumer<String, MqttMessage> messageHandler;

    public MaestroMqttCallback(Consumer<Boolean> resubscribe, BiConsumer<String, MqttMessage> messageHandler) {
        this.resubscribe = resubscribe;
        this.messageHandler = messageHandler;
    }


    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        logger.info("Connection to {} completed (reconnect = {})", serverUri, reconnect);

        resubscribe.accept(reconnect);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.warn("Connection lost");
    }


    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        logger.trace("Message arrived on topic {}", s);
        messageHandler.accept(s, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
