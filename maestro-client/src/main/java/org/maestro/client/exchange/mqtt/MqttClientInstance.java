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

package org.maestro.client.exchange.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.maestro.client.exchange.MqttUtil;
import org.maestro.common.URLUtils;
import org.maestro.common.exceptions.MaestroConnectionException;

import java.util.UUID;

public class MqttClientInstance {
    private static MqttClientInstance instance = null;
    private MqttClient client;
    private final String id;

    private MqttClientInstance(final String url) {
        String adjustedUrl = URLUtils.sanitizeURL(url);

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        this.id = clientId;
        try {
            client = new MqttClient(adjustedUrl, "maestro.exchange." + clientId, memoryPersistence);

            Runtime.getRuntime().addShutdownHook(new Thread(this::terminate));
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable create a MQTT client instance : " + e.getMessage(),
                    e);
        }

    }

    public synchronized static MqttClientInstance getInstance(final String url) {
        String noReuse = System.getProperty("maestro.mqtt.no.reuse");

        if (noReuse == null) {
            if (instance == null) {
                instance = new MqttClientInstance(url);
            }
        }
        else {
            return new MqttClientInstance(url);
        }

        return instance;
    }

    public static MqttConnectOptions getConnectionOptions() {
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);

        return connOpts;
    }

    public MqttClient getClient() {
        return client;
    }

    public String getId() {
        return id;
    }

    private synchronized void terminate() {
        MqttUtil.terminate(client);
    }
}
