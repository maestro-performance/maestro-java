/*
 *  Copyright ${YEAR} ${USER}
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MaestroCollector implements MqttCallback {
    private MqttClient mqttClient;

    public MaestroCollector(final String url) throws MqttException {
        // The client uses the mqtt://<host> url format so it's the same as the C client
        String adjustedUrl = StringUtils.replace(url, "mqtt", "tcp");

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        mqttClient = new MqttClient(adjustedUrl, "maestro-java-collector" + clientId, memoryPersistence);
    }

    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);

        mqttClient.connect();
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

    public void subscribe() throws MqttException {
        mqttClient.setCallback(this);
        mqttClient.subscribe(MaestroTopics.MAESTRO_TOPICS);
    }

    public void connectionLost(Throwable throwable) {

    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("Message arrived: ");
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

}
