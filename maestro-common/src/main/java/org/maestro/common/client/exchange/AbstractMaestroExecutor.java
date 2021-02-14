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

package org.maestro.common.client.exchange;

import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Provides an abstract interface that can be used to receive data from a Maestro broker.
 */
public class AbstractMaestroExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMaestroExecutor.class);

    private final MaestroPeer maestroPeer;

    /**
     * Constructor
     * @param maestroPeer a Maestro peer object is capable of exchange maestro data.
     * @throws MaestroConnectionException if unable to connect or subscribe
     */
    protected AbstractMaestroExecutor(final MaestroPeer maestroPeer) throws MaestroConnectionException {
        this.maestroPeer = maestroPeer;
    }


    /**
     * Get the Maestro peer
     * @return the maestro peer object
     */
    protected MaestroPeer getMaestroPeer() {
        return maestroPeer;
    }

    /**
     * Start running the executor
     * @param topics the list of topics associated with this executor
     * @throws MaestroConnectionException if unable to connect to the broker and subscribe to the topics
     */
    public void start(final String[] topics) throws MaestroConnectionException {
        logger.debug("Connecting the Maestro broker");

        maestroPeer.connect();
        if (topics == null) {
            logger.error("Trying to subscribe to a null topic");
        }
        else {
            maestroPeer.subscribe(topics);
        }
    }

    /**
     * Start running the executor
     * @param topics the list of topics associated with this executor
     * @param connectionRetries the number of times to retry connecting in case of errors
     * @param retryDelay the amount of time to wait between each connection retry
     * @throws MaestroConnectionException if unable to connect to the broker and subscribe to the topics
     */
    public void start(final String[] topics, int connectionRetries, long retryDelay) throws MaestroConnectionException {
        logger.debug("Connecting the Maestro broker");

        boolean connected = false;
        do {
            try {
                maestroPeer.connect();
                connected = true;
                System.out.println("Connected to the Maestro Broker");
                break;
            }
            catch (MaestroConnectionException e) {
                logger.error("Maestro did not connect. Waiting and retrying {} more times", connectionRetries);
                connectionRetries--;

                if (connectionRetries > 0) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException e1) {
                        break;
                    }
                }
                else {
                    System.err.println(e.getMessage());
                }
            }
        } while (connectionRetries > 0);

        if (connected) {
            if (topics == null) {
                logger.error("Trying to subscribe to a null topic");
            } else {
                maestroPeer.subscribe(topics);
            }
        }
        else {
            throw new MaestroConnectionException("The peer did not connect to the Maestro Broker");
        }
    }

    /**
     * Runs the executor
     */
    public final void run() {
        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();

        try {
            lock.lock();
            while (!Thread.currentThread().isInterrupted() && maestroPeer.isRunning()) {
                try {
                    condition.await(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.info("Interrupted. Aborting");
                    break;
                }
            }
        }
        finally {
            lock.unlock();
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
