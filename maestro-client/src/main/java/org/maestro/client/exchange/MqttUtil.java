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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttUtil {
    private static final Logger logger = LoggerFactory.getLogger(MqttUtil.class);

    /**
     * MQTT connection shutdown utility
     * @param client MQTT client
     */
    public static void terminate(final MqttClient client) {
        logger.info("Finalizing Maestro peer connection");
        if (client != null) {
            try {
                if (client.isConnected()) {
                    try {
                        client.disconnect();
                    } catch (MqttException e) {
                        logger.warn("Error trying to disconnect from Maestro Broker: {}", e.getMessage(), e);
                    }
                }
            }
            finally {
                try {
                    client.close();
                } catch (MqttException e) {
                    logger.warn("Error trying to close MQTT resources: {}", e.getMessage(), e);
                }
            }
        }
    }
}
