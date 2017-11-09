/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.maestro.client;

import net.orpiske.mpt.common.URLUtils;
import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class MaestroClient {
    private static final Logger logger = LoggerFactory.getLogger(MaestroClient.class);

    private MqttClient mqttClient;

    public MaestroClient(final String url) throws MaestroConnectionException {
        String adjustedUrl = URLUtils.sanizeURL(url);

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        try {
            mqttClient = new MqttClient(adjustedUrl, "maestro-java-" + clientId, memoryPersistence);
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable create a MQTT client instance : " + e.getMessage(),
                    e);
        }
    }

    public void connect() throws MaestroConnectionException {
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);

        try {
            mqttClient.connect();
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to establish a connection to Maestro: " + e.getMessage(), e);
        }
    }

    public void disconnect() throws MaestroConnectionException {
        try {
            mqttClient.disconnect();
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to disconnect cleanly from Maestro: " + e.getMessage(), e);
        }
    }

    public void publish(final String topic, final MaestroNote note) throws MaestroConnectionException, IOException {
        byte[] bytes = note.serialize();

        try {
            mqttClient.publish(topic, bytes, 0, false);
        } catch (MqttException e) {
            throw new MaestroConnectionException("Unable to publish message: " + e.getMessage(), e);
        }
    }

}
