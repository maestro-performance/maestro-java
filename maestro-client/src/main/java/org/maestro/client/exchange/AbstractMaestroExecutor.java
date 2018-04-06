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

import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides an abstract interface that can be used to receive data from a Maestro broker.
 */
public class AbstractMaestroExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollectorExecutor.class);

    private final AbstractMaestroPeer maestroPeer;

    /**
     * Constructor
     * @param maestroPeer a Maestro peer object is capable of exchange maestro data.
     * @throws MaestroConnectionException if unable to connect or subscribe
     */
    public AbstractMaestroExecutor(final AbstractMaestroPeer maestroPeer) throws MaestroConnectionException {
        this.maestroPeer = maestroPeer;
    }


    /**
     * Get the Maestro peer
     * @return the maestro peer object
     */
    protected AbstractMaestroPeer getMaestroPeer() {
        return maestroPeer;
    }

    /**
     * Start running the executor
     * @param topics the list of topics associated with this executor
     * @throws MaestroConnectionException if unable to connect to the broker and subscribe to the topics
     */
    public void start(final String[] topics) throws MaestroConnectionException {
        logger.debug("Connecting the maestro broker");

        maestroPeer.connect();
        maestroPeer.subscribe(topics);
    }

    /**
     * Runs the executor
     */
    public final void run() {
        while (maestroPeer.isRunning()) {
            try {
                logger.trace("Waiting for data ...");

                if (!maestroPeer.isConnected()) {
                    logger.error("Disconnected from the broker: reconnecting");
                    maestroPeer.connect();
                }

                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Stops the executor
     */
    public void stop() {
        try {
            logger.debug("Disconnecting the peer");
            maestroPeer.disconnect();
        } catch (MaestroConnectionException e) {
            logger.debug(e.getMessage(), e);
        }
    }

}
