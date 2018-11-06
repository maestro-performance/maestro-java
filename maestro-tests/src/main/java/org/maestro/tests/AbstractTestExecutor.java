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

package org.maestro.tests;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Role;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MessageCorrelation;
import org.maestro.common.client.notes.Test;
import org.maestro.common.client.notes.WorkerStartOptions;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.TryAgainException;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.maestro.client.Maestro.exec;

/**
 * A simple test executor that should be extensible for most usages
 */
public abstract class AbstractTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final Maestro maestro;

    private volatile boolean running = false;
    private Instant startTime;

    protected AbstractTestExecutor(final Maestro maestro) {
        this.maestro = maestro;
        logger.trace("Creating an abstract test executor");
    }

    protected Maestro getMaestro() {
        return maestro;
    }

    private void tryTestStart(final Test test) {
        int retries = 6;

        do {
            try {
                exec(maestro::startTest, MaestroTopics.PEER_TOPIC, test);
                break;
            } catch (TryAgainException e) {
                logger.warn("Waiting 10 seconds because the test cannot be started at this moment : {}", e.getMessage());
                try {
                    Thread.sleep(10000);
                    retries--;
                    if (retries == 0) {
                        throw e;
                    }
                } catch (InterruptedException e1) {
                    logger.warn("Interrupted while waiting for the successful test start", e1);
                    throw e;
                }
            }
        } while (retries > 0);
    }

    protected void testStart(final Test test) {
        tryTestStart(test);

        running = true;
        startTime = Instant.now();
    }

    public void testStop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Start connected peers
     * @param testProfile the test profile in use
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected void startServices(final AbstractTestProfile testProfile, final DistributionStrategy distributionStrategy)
            throws MaestroConnectionException
    {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();

        for (PeerEndpoint endpoint : endpoints) {
            String destination = endpoint.getDestination();
            logger.debug("Sending the start request to the {} peers on the destination {}", endpoint.getRole(), destination);

            if (endpoint.getRole() == Role.SENDER) {
                final String senderName = "JmsSender";

                try {
                    exec(maestro::startWorker, destination, new WorkerStartOptions(senderName), 3);
                }
                catch (NotEnoughRepliesException e) {
                    logger.warn("While starting the sender: {}", e.getMessage());
                }
            }
            else {
                if (endpoint.getRole() == Role.RECEIVER) {
                    final String receiverName = "JmsReceiver";

                    try {
                        exec(maestro::startWorker, destination, new WorkerStartOptions(receiverName));
                    }
                    catch (NotEnoughRepliesException e) {
                        logger.warn("While starting the receiver: {}", e.getMessage());
                    }
                }
            }
        }

        try {
            final String inspectorName = testProfile.getInspectorName();

            if (inspectorName != null) {
                exec(maestro::startInspector, inspectorName);
            }
            else {
                logger.info("There is no inspector setup for this test");
            }
        }
        catch (NotEnoughRepliesException e) {
            logger.warn("While starting the inspector: {}", e.getMessage());
        }
    }

    private void checkReplies(List<? extends MaestroNote> replies) {
        Maestro.checkReplies(replies, "stop");
    }

    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected final void stopServices(final DistributionStrategy distributionStrategy) throws MaestroConnectionException {
        logger.info("Requesting all Maestro peers to stop");

        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();
        List<MessageCorrelation> correlations = new LinkedList<>();

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.SENDER) {
                MessageCorrelation correlation = getMaestro().stopWorkerAsync(peerEndpoint.getDestination());

                correlations.add(correlation);
            }
        }

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.RECEIVER) {
                MessageCorrelation correlation = getMaestro().stopWorkerAsync(peerEndpoint.getDestination());

                correlations.add(correlation);
            }
        }

        MessageCorrelation correlation = getMaestro().stopWorkerAsync(MaestroTopics.peerTopic(Role.INSPECTOR));

        correlations.add(correlation);

        maestro.waitFor(correlations).thenAccept(this::checkReplies);

    }


    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    public void stopServices() throws MaestroConnectionException {
        try {
            exec(maestro::stopWorker, MaestroTopics.WORKERS_TOPIC, 5);
        }
        catch (NotEnoughRepliesException e) {
            logger.warn("While stopping the peers: {}", e.getMessage(), e);
        }

        try {
            exec(maestro::stopInspector, MaestroTopics.peerTopic(Role.INSPECTOR));
        }
        catch (NotEnoughRepliesException e) {
            logger.warn("While stopping the inspector: {}", e.getMessage());
        }
    }


    protected boolean isTestFailed(final MaestroNote note) {
        if (note instanceof TestFailedNotification) {
            final TestFailedNotification testFailedNotification = (TestFailedNotification) note;
            logger.error("Test failed on {}: {}", testFailedNotification.getPeerInfo().prettyName(),
                    testFailedNotification.getMessage());
            return true;
        }

        if (note instanceof InternalError) {
            final InternalError internalError = (InternalError) note;
            logger.error("Test error occurred on {}: {}", internalError.getPeerInfo().prettyName(),
                    internalError.getMessage());
            return true;
        }

        return false;
    }

    protected boolean isFailed(final MaestroNote note) {
        boolean success = true;

        if (note instanceof DrainCompleteNotification) {
            success = ((DrainCompleteNotification) note).isSuccessful();
            if (!success) {
                logger.error("Drained failed for {}", ((DrainCompleteNotification) note).getPeerInfo().prettyName());
            }
        }

        return success;
    }


    protected long getTimeout(final AbstractTestProfile testProfile) {
        return testProfile.getEstimatedCompletionTime() + CompletionTime.getDeadline();
    }

    protected void drain(final PeerSet peerSet) {
        long drainDeadline = config.getLong("client.drain.deadline.secs", 60);

        try {
            long numReceivers = peerSet.count(Role.RECEIVER);
            logger.debug("Waiting for {} drain notifications", numReceivers);

            final List<? extends MaestroNote> drainReplies = getMaestro()
                    .waitForDrain((int) numReceivers)
                    .get(drainDeadline, TimeUnit.SECONDS);

            if (drainReplies.size() != numReceivers) {
                logger.warn("Received only {} of {} expected drain notifications within {} seconds",
                        drainReplies.size(), numReceivers, drainDeadline);
            }

            drainReplies.forEach(this::isFailed);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error checking the draining status: {}", e.getMessage(), e);
        } catch (TimeoutException e) {
            logger.warn("Did not receive a drain response within {} seconds", drainDeadline);
        }
    }
}
