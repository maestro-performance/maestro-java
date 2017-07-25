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

package net.orpiske.mpt.main;

import net.orpiske.mpt.maestro.client.MaestroClient;
import net.orpiske.mpt.maestro.client.MaestroCollector;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.notes.FlushRequest;
import net.orpiske.mpt.maestro.notes.PingRequest;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class Maestro {
    private String url;
    private MaestroClient maestroClient = null;
    private MaestroCollector maestroCollector = null;

    public Maestro(final String url) throws MqttException {
        this.url = url;

        maestroCollector = new MaestroCollector(url);
        maestroCollector.connect();
        maestroCollector.subscribe();

        maestroClient = new MaestroClient(url);
        maestroClient.connect();
    }

    public void stop() throws MqttException {
        maestroClient.disconnect();
    }

    public void flushRequest() throws MqttException, IOException {
        FlushRequest maestroNote = new FlushRequest();

        maestroClient.publish(MaestroTopics.MAESTRO_TOPIC, maestroNote);
    }

    public void pingRequest() throws MqttException, IOException {
        PingRequest maestroNote = new PingRequest();

        maestroClient.publish(MaestroTopics.MAESTRO_TOPIC, maestroNote);
    }
}
