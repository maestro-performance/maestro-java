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

package net.orpiske.mpt.maestro;

import net.orpiske.mpt.maestro.client.MaestroClient;
import net.orpiske.mpt.maestro.client.MaestroCollectorExecutor;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.notes.FlushRequest;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.PingRequest;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.List;

public class Maestro {
    private MaestroClient maestroClient = null;
    private MaestroCollectorExecutor collectorExecutor;
    private Thread collectorThread;

    public Maestro(final String url) throws MqttException {
        collectorExecutor = new MaestroCollectorExecutor(url);

        maestroClient = new MaestroClient(url);
        maestroClient.connect();

        collectorThread = new Thread(collectorExecutor);
        collectorThread.start();
    }

    public void stop() throws MqttException {
        maestroClient.disconnect();
        collectorExecutor.stop();
        try {
            collectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void flushRequest() throws MqttException, IOException {
        FlushRequest maestroNote = new FlushRequest();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }

    public void pingRequest() throws MqttException, IOException {
        PingRequest maestroNote = new PingRequest();

        maestroClient.publish(MaestroTopics.ALL_DAEMONS, maestroNote);
    }

    public List<MaestroNote> collect() {
        return collectorExecutor.collect();
    }

    private boolean hasReplies(List<?> replies) {
        return (replies != null && replies.size() > 0);
    }

    public List<MaestroNote> collect(long wait, int retries) {
        List<MaestroNote> replies = null;

        do {
            replies = collectorExecutor.collect();

            if (hasReplies(replies)) {
                break;
            }

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retries--;
        } while (retries > 0);

        return replies;
    }
}
