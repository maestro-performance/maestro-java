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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.maestro.common.URLUtils;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;


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

    private MqttClient inboundEndPoint;
    protected String id;
    private final MaestroNoteDeserializer<? extends T> deserializer;

    public AbstractMaestroPeer(final String url, MaestroNoteDeserializer<? extends T> deserializer) throws MaestroConnectionException {

        String adjustedUrl = URLUtils.sanitizeURL(url);

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        this.id = clientId;

        try {
            inboundEndPoint = new MqttClient(adjustedUrl, "maestro.inbound." + clientId, memoryPersistence);
            inboundEndPoint.setCallback(this);

            // Runtime.getRuntime().addShutdownHook(new Thread(this::terminate));
            Runtime.getRuntime().addShutdownHook(new Thread(this::terminate));
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable create a MQTT client instance : " + e.getMessage(),
                    e);
        }

        this.deserializer = deserializer;
    }

    private void terminate() {
        MqttUtil.terminate(inboundEndPoint);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void connectionLost(Throwable throwable) {
        logger.warn("Connection lost");
    }



    public boolean isConnected() {
        return inboundEndPoint.isConnected();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void connect() throws MaestroConnectionException {
        logger.info("Connecting to Maestro Broker");
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);
        connOpts.setKeepAliveInterval(15000);
        connOpts.setAutomaticReconnect(true);

        try {
            inboundEndPoint.connect(connOpts);
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
        logger.debug("Disconnecting from Maestro Broker");

        try {
            inboundEndPoint.disconnect();
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to disconnect cleanly from Maestro: " + e.getMessage(), e);
        }
    }

    public void subscribe(final String topic, int qos) {
        try {
            inboundEndPoint.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new MaestroConnectionException("Unable to subscribe to Maestro topics: " + e.getMessage(), e);
        }
    }

    public void subscribe(final String[] topics) throws MaestroConnectionException {
        logger.debug("Subscribing to maestro topics {}", Arrays.toString(topics));

        int qos[] = new int[topics.length];

        for (int i = 0; i < topics.length; i++) {
            qos[i] = 0;
        }

        try {
            inboundEndPoint.subscribe(topics, qos);
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to subscribe to Maestro topics: " + e.getMessage(), e);
        }
    }

    public void messageArrived(String s, MqttMessage mqttMessage) {
        logger.trace("Message arrived on topic {}", s);

        byte[] payload = mqttMessage.getPayload();

        try {
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
        logger.warn("Connection to {} completed (reconnect = {})", serverUri, reconnect);
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
