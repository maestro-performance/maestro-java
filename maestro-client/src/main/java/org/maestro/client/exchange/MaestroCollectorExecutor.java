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

package org.maestro.client.exchange;

import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplified executor to be used along with a maestro client. It is meant to run in a thread,
 * along side with the sending peer. Received messages can be collected via collect method.
 */
public class MaestroCollectorExecutor extends AbstractMaestroExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollectorExecutor.class);

    /**
     * Constructor
     * @param url Maestro broker URL
     * @throws MaestroConnectionException if unable to connect to the Maestro broker
     */
    public MaestroCollectorExecutor(final String url) throws MaestroConnectionException {
        super(new MaestroCollector(url));

        logger.trace("Created a new maestro collector executor");

        super.start(MaestroTopics.MAESTRO_TOPICS);
    }


    /**
     * Collect the messages received in background
     */
    public void clear() {
        MaestroCollector maestroCollector = (MaestroCollector) super.getMaestroPeer();

        maestroCollector.clear();
    }

    public void stop() {
        MaestroCollector maestroCollector = (MaestroCollector) super.getMaestroPeer();

        maestroCollector.setRunning(false);

        super.stop();
    }

    public MaestroCollector getCollector() {
        return (MaestroCollector) getMaestroPeer();
    }
}
