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

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MaestroCollectorExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollectorExecutor.class);

    private MaestroCollector maestroCollector = null;
    private volatile boolean exit = false;

    public MaestroCollectorExecutor(final String url) throws MaestroConnectionException {
        maestroCollector = new MaestroCollector(url);

        logger.debug("Connecting the collector");
        maestroCollector.connect();

        logger.debug("Subscribing the collector");
        maestroCollector.subscribe(MaestroTopics.MAESTRO_TOPICS);
    }

    public void run() {
        while (!exit) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void stop() {
        try {
            maestroCollector.disconnect();
        } catch (MaestroConnectionException e) {
            logger.debug(e.getMessage(), e);
        }

        exit = true;
    }

    public List<MaestroNote> collect() {
        return maestroCollector.collect();
    }
}
