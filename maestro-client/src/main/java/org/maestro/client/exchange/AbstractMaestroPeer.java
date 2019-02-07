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

package org.maestro.client.exchange;

import org.eclipse.paho.client.mqttv3.*;
import org.maestro.client.exchange.mqtt.MqttClientInstance;
import org.maestro.client.exchange.support.PeerInfo;
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


/**
 * Is a base class that implements the basic operations of any peer connect to Maestro (not just senders and receivers).
 * For example, a Collector instance that keeps reading the data from the Maestro broker is an specialization of this
 * class as is the MaestroWorkerManager that handles the requests from the Maestro broker and manages the
 * sender/receiver workers
 * @param <T>
 *
 * TODO: configure LWT
 */
public abstract class AbstractMaestroPeer<T extends MaestroNote> implements MqttCallbackExtended {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMaestroPeer.class);
    private static final Set<Subscription> subscriptions = new LinkedHashSet<>();

    private final MqttClient inboundEndPoint;
    private final MaestroNoteDeserializer<? extends T> deserializer;
    private final PeerInfo peerInfo;

    private final ExecutorService messageHandlerService = Executors.newSingleThreadExecutor();

    protected AbstractMaestroPeer(final String url, final PeerInfo peerInfo, MaestroNoteDeserializer<? extends T> deserializer) throws MaestroConnectionException {
        this(MqttClientInstance.getInstance(url).getClient(), peerInfo, deserializer);
    }

    protected AbstractMaestroPeer(final MqttClient inboundEndPoint, final PeerInfo peerInfo, MaestroNoteDeserializer<? extends T> deserializer) throws MaestroConnectionException {
        this.peerInfo = peerInfo;

        this.inboundEndPoint = inboundEndPoint;
        this.inboundEndPoint.setCallback(this);

        this.deserializer = deserializer;
    }


    protected PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public String getId() {
        return inboundEndPoint.getClientId();
    }

    public void connectionLost(Throwable throwable) {
        if (isRunning()) {
            logger.warn("Connection lost");
        }
    }

    public boolean isConnected() {
        return inboundEndPoint.isConnected();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void connect() throws MaestroConnectionException {
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

    public void reconnect(final String[] topics) {
        try {
            connect();
            subscribe(topics);
        }
        catch (MaestroConnectionException e) {
            logger.warn("Unable to connect: {}", e.getMessage());
        }
    }

    public void disconnect() throws MaestroConnectionException {
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

    public void subscribe(final String topic, int qos) {
        try {
            inboundEndPoint.subscribe(topic, qos);

            subscriptions.add(new Subscription(topic, qos));
        } catch (MqttException e) {
            subscriptions.remove(new Subscription(topic, qos));
            throw new MaestroConnectionException("Unable to subscribe to Maestro topics: " + e.getMessage(), e);
        }
    }

    public void subscribe(final String[] topics) throws MaestroConnectionException {
        subscribe(topics, MqttServiceLevel.AT_LEAST_ONCE);
    }

    public void subscribe(final String[] topics, int allQos) throws MaestroConnectionException {
        logger.debug("Subscribing to maestro topics {}", Arrays.toString(topics));

        for (String topic : topics) {
            subscribe(topic, allQos);
        }
    }


    public void messageArrived(final String s, final MqttMessage mqttMessage) {
        messageHandlerService.submit(() -> handleMessage(s, mqttMessage));
    }

    private void handleMessage(String s, MqttMessage mqttMessage) {
        logger.trace("Message arrived on topic {}", s);

        try {
            final byte[] payload = mqttMessage.getPayload();

            final T note = deserializer.deserialize(payload);
            logger.trace("Message type: {}", note.getClass());

            if (!note.hasNext()) {
                noteArrived(note);
            }
        } catch (MalformedNoteException e) {
            logger.error("Invalid message type: {}", e.getMessage(), e);
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unhandled exception: {}", e.getMessage(), e);
        }
    }


    @Override
    public void connectComplete(boolean reconnect, final String serverUri) {
        logger.info("Connection to {} completed (reconnect = {})", serverUri, reconnect);

        if (reconnect) {
            logger.info("Resubscribing to topics that were previously subscribed");
            for (Subscription subscription : subscriptions) {
                subscribe(subscription.getTopic(), subscription.getQos());
            }
        }
    }

    /**
     * The entry point for handling Maestro messages
     * @param note the note that arrived
     *
     * @throws MaestroConnectionException for Maestro related errors
     */
    protected abstract void noteArrived(T note) throws MaestroConnectionException;

    public abstract boolean isRunning();


}
