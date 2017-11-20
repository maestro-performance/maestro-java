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

package net.orpiske.mpt.maestro.test.scripts.support;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.io.FileUtils;

import java.net.URI;
import java.net.URL;

public class MiniBroker {
    Thread thread;
    BrokerInstance brokerInstance;

    class BrokerInstance implements Runnable {
        BrokerService broker = new BrokerService();
        boolean running = true;

        public BrokerInstance() throws Exception {
            TransportConnector mqttConnector = new TransportConnector();

            URL url = this.getClass().getResource("/");

            /*
            Check if we are running it in within the jar, in which case we
            won't be able to use its location ...
            */

            String path;

            if (url == null) {
                /*
                 ... and, if that's the case, we use the OS temporary directory
                 for the data directory
                 */
                path = FileUtils.getTempDirectoryPath();
            }
            else {
                path = url.getPath();
            }

            broker.setDataDirectory(path);
            broker.setPersistent(false);

            mqttConnector.setUri(new URI("mqtt://localhost:1883"));
            mqttConnector.setName("mqtt");
            broker.addConnector(mqttConnector);

            TransportConnector amqpConnector = new TransportConnector();
            amqpConnector.setUri(new URI("amqp://localhost:5672"));
            amqpConnector.setName("amqp");
            broker.addConnector(amqpConnector);

            // broker.setStartAsync(true);
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void run() {
            try {
                broker.start();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public boolean isStarted() {
            return broker.isStarted();
        }
    }


    public void start() throws Exception {
        brokerInstance = new BrokerInstance();
        thread = new Thread(brokerInstance);

        thread.start();
    }

    public void stop() {
        brokerInstance.setRunning(false);
    }

    public boolean isStarted() {
        return brokerInstance.isStarted();
    }
}
