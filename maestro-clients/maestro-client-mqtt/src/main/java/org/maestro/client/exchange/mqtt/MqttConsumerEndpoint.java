package org.maestro.client.exchange.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.MaestroNoteDeserializer;
import org.maestro.common.client.ServiceLevel;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MqttConsumerEndpoint<T extends MaestroNote> implements ConsumerEndpoint<T> {
    private static final Logger logger = LoggerFactory.getLogger(MqttConsumerEndpoint.class);
    private static final Set<Subscription> subscriptions = new LinkedHashSet<>();

    private final MqttClient inboundEndPoint;

    private final ExecutorService messageHandlerService = Executors.newSingleThreadExecutor();
    private final MaestroNoteDeserializer<? extends T> deserializer;
    private Consumer<T> noteArrivedConsumer;

    public MqttConsumerEndpoint(final String url, MaestroNoteDeserializer<? extends T> deserializer) throws MaestroConnectionException {
        this(MqttClientInstance.getInstance(url).getClient(), deserializer, null);
    }

    protected MqttConsumerEndpoint(final MqttClient inboundEndPoint, MaestroNoteDeserializer<? extends T> deserializer, Consumer<T> noteArrivedConsumer) throws MaestroConnectionException {
        this.inboundEndPoint = inboundEndPoint;
        this.inboundEndPoint.setCallback(new MaestroMqttCallback(this::resubscribe, this::handleMessage));
        this.deserializer = deserializer;
        this.noteArrivedConsumer = noteArrivedConsumer;
    }

    @Override
    public String getClientId() {
        return inboundEndPoint.getClientId();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect() {
        logger.info("Connecting to Maestro Broker");
        try {
            final MqttConnectOptions connOpts = MqttClientInstance.getConnectionOptions();

            if (!inboundEndPoint.isConnected()) {
                inboundEndPoint.connect(connOpts);
            }

            logger.debug("Connected to Maestro Broker");
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to establish a connection to Maestro: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        logger.info("Disconnecting from Maestro Broker");
        messageHandlerService.shutdown();

        try {
            if (!messageHandlerService.awaitTermination(1, TimeUnit.SECONDS)) {
                messageHandlerService.shutdownNow();
                if (!messageHandlerService.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.warn("Message handler service did not stop cleanly");
                }
            }
        }
        catch (InterruptedException e) {
            logger.trace("Interrupted while waiting for the message handler service to shutdown");
        }
        finally {
            try {
                if (inboundEndPoint.isConnected()) {
                    inboundEndPoint.disconnect();
                }
            }
            catch (MqttException e) {
                throw new MaestroConnectionException("Unable to disconnect cleanly from Maestro: " + e.getMessage(), e);
            }
        }
    }


    private void resubscribe(boolean reconnect) {
        if (reconnect) {
            logger.info("Resubscribing to topics that were previously subscribed");
            try {
                for (Subscription subscription : subscriptions) {
                    inboundEndPoint.subscribe(subscription.getTopic(), subscription.getQos());
                }
            }
            catch (MqttException e) {
                logger.error("Unable to resubscribe: {}", e.getMessage(), e);

                disconnect();
            }
        }
    }

    private void subscribe(final String topic, int qos) {
        try {
            inboundEndPoint.subscribe(topic, qos);

            subscriptions.add(new Subscription(topic, qos));
        } catch (MqttException e) {
            subscriptions.remove(new Subscription(topic, qos));
            throw new MaestroConnectionException("Unable to subscribe to Maestro topics: " + e.getMessage(), e);
        }
    }

     private void subscribe(final String[] topics, int allQos) throws MaestroConnectionException {
        logger.debug("Subscribing to maestro topics {}", Arrays.toString(topics));

        for (String topic : topics) {
            subscribe(topic, allQos);
        }
    }

    public void subscribe(final String[] topics) throws MaestroConnectionException {
        subscribe(topics, ServiceLevel.AT_LEAST_ONCE.getLevel());
    }

    private void handleMessage(String s, byte[] payload) {
        logger.trace("Message arrived on topic {}", s);

        try {
            final T note = deserializer.deserialize(payload);
            logger.trace("Message type: {}", note.getClass());

            if (!note.hasNext()) {
                noteArrivedConsumer.accept(note);
            }
        } catch (MalformedNoteException e) {
            logger.error("Invalid message type: {}", e.getMessage(), e);
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unhandled exception: {}", e.getMessage(), e);
        }
    }

    private void handleMessage(String s, MqttMessage mqttMessage) {
        final byte[] payload = mqttMessage.getPayload();

        messageHandlerService.submit(() -> handleMessage(s, payload));
    }

    @Override
    public void setConsumer(Consumer<T> noteArrivedConsumer) {
        this.noteArrivedConsumer = noteArrivedConsumer;
    }
}
