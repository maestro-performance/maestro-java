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

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.common.URLUtils;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class MaestroMqttClient implements MaestroClient {
    private static final Logger logger = LoggerFactory.getLogger(MaestroClient.class);

    private MqttClient mqttClient;

    /**
     * Constructor
     * @param url Maestro broker URL
     * @throws MaestroException if unable to create the client
     */
    public MaestroMqttClient(final String url) throws MaestroException {
        String adjustedUrl = URLUtils.sanitizeURL(url);

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        try {
            mqttClient = new MqttClient(adjustedUrl, "maestro-java-" + clientId, memoryPersistence);

            Runtime.getRuntime().addShutdownHook(new Thread(this::terminate));
        }
        catch (MqttException e) {
            throw new MaestroException("Unable create a MQTT client instance : " + e.getMessage(),
                    e);
        }
    }

    private void terminate() {
        MqttUtil.terminate(mqttClient);
    }

    /**
     * Connect to the maestro broker
     * @throws MaestroConnectionException if unable to connect to the broker
     */
    public void connect() throws MaestroConnectionException {
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);

        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect(connOpts);
            }
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to establish a connection to Maestro: " + e.getMessage(), e);
        }
    }


    /**
     * Disconnects from the maestro broker
     * @throws MaestroConnectionException if failed to disconnect cleanly (should be safe to ignore in most cases)
     */
    public void disconnect() throws MaestroConnectionException {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to disconnect cleanly from Maestro: " + e.getMessage(), e);
        }
    }


    /**
     * Publishes a message in the broker. This is normally used for publishing notifications,
     * because some of them are set as retained in the broker
     *
     * @param topic the topic to publish the message
     * @param note  the maestro note to publish
     * @param qos MQTT QoS
     * @param retained MQTT retained flag
     * @throws MaestroConnectionException if failed to publish the message
     * @throws MalformedNoteException     in case of other I/O errors
     */
    protected void publish(final String topic, final MaestroNote note, int qos, boolean retained) throws
            MalformedNoteException, MaestroConnectionException
    {
        do {
            final byte[] bytes;
            try {
                bytes = note.serialize();
            } catch (IOException e) {
                throw new MalformedNoteException(e.getMessage());
            }

            try {
                if (!mqttClient.isConnected()) {
                    logger.warn("The client is disconnected ... reconnecting");
                    mqttClient.reconnect();
                }

                MqttTopic mqttTopic = mqttClient.getTopic(topic);

                mqttTopic.publish(bytes, qos, retained);
                note.next();
            } catch (MqttException e) {
                throw new MaestroConnectionException("Unable to publish message: " + e.getMessage(), e);
            }
        } while (note.hasNext());
    }

    /**
     * Publishes a message in the broker. This is normally used for publishing notifications,
     * because some of them are set as retained in the broker
     *
     * @param topic the topic to publish the message
     * @param note  the maestro note to publish
     * @param qos MQTT QoS
     * @param retained MQTT retained flag
     * @param postProcessCallback A call back action to be executed after the message was sent
     * @throws MaestroConnectionException if failed to publish the message
     * @throws MalformedNoteException     in case of other I/O errors
     */
    protected void publish(final String topic, final MaestroNote note, int qos, boolean retained,
                           final MaestroNoteCallback postProcessCallback) throws
            MalformedNoteException, MaestroConnectionException
    {
        do {
            final byte[] bytes;
            try {
                bytes = note.serialize();
            } catch (IOException e) {
                throw new MalformedNoteException(e.getMessage());
            }

            try {
                if (!mqttClient.isConnected()) {
                    logger.warn("The client is disconnected ... reconnecting");
                    mqttClient.reconnect();
                }

                MqttTopic mqttTopic = mqttClient.getTopic(topic);

                mqttTopic.publish(bytes, qos, retained);
                if (postProcessCallback != null) {
                    postProcessCallback.call(note);
                }

                note.next();
            } catch (MqttException e) {
                throw new MaestroConnectionException("Unable to publish message: " + e.getMessage(), e);
            }
        } while (note.hasNext());
    }

    /**
     * Publishes a message in the broker
     *
     * @param topic the topic to publish the message
     * @param note  the maestro note to publish
     * @throws MaestroConnectionException if failed to publish the message
     * @throws MalformedNoteException     in case of other I/O errors
     */
    public void publish(final String topic, final MaestroNote note) throws MalformedNoteException, MaestroConnectionException {
        publish(topic, note, 0, false);
    }


    /**
     * Subscribe to a topic
     * @param topic the topic to subscribe
     * @param qos the QOS for the topic
     */
    public void subscribe(final String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new MaestroConnectionException("Unable to subscribe to topic " + topic + " :"  + e.getMessage(), e);
        }
    }

    /**
     * Unsubscribe from a topic
     * @param topic the topic to unsubscribe from
     */
    public void unsubscribe(final String topic) {
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            throw new MaestroConnectionException("Unable to unsubscribe from topic " + topic + " :"  + e.getMessage(), e);
        }
    }
}
