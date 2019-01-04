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
import org.maestro.common.client.notes.TestExecutionInfo;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.maestro.client.Maestro.exec;
import static org.maestro.tests.utils.IgnoredErrorUtils.isIgnored;

/**
 * A simple test executor that should be extensible for most usages
 */
public abstract class AbstractTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final Maestro maestro;

    private volatile boolean running = false;
    private Instant startTime;

    /**
     * Constructor
     * @param maestro the maestro client instance
     */
    protected AbstractTestExecutor(final Maestro maestro) {
        this.maestro = maestro;
        logger.trace("Creating an abstract test executor");
    }

    /**
     * Gets the Maestro client instance
     * @return the Maestro client instance
     */
    protected Maestro getMaestro() {
        return maestro;
    }

    private void tryTestStart(final TestExecutionInfo testExecutionInfo) {
        int retries = 6;

        do {
            try {
                exec(maestro::startTest, MaestroTopics.PEER_TOPIC, testExecutionInfo);
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

    protected void testStart(final TestExecutionInfo testExecutionInfo) {
        tryTestStart(testExecutionInfo);

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

            if (inspectorName != null && !inspectorName.isEmpty()) {
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
        if (replies.size() == 0) {
            logger.error("Not enough replies when trying to execute a command on the test cluster");
        }

        for (MaestroNote reply : replies) {
            if (reply instanceof InternalError) {
                InternalError ie = (InternalError) reply;
                logger.error("Error while stopping the workers: {}", ie.getMessage());
            }
        }
    }

    private void verifyStopCommand(CompletableFuture<List<? extends MaestroNote>> completableFuture) {
        if (!completableFuture.isDone()) {
            logger.trace("Still waiting for the stop worker replies");
        }
    }

    /**
     * Stop connected peers
     * @param distributionStrategy the distribution strategy to use for the peers of the test
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected final void stopServices(final DistributionStrategy distributionStrategy) throws MaestroConnectionException {
        logger.info("Requesting all Maestro peers to stop");

        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();
        List<CompletableFuture<List<? extends MaestroNote>>> futures = new LinkedList<>();

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.SENDER) {
                CompletableFuture<List<? extends MaestroNote>> stopWorkerFuture = getMaestro()
                        .stopWorker(peerEndpoint.getDestination());

                stopWorkerFuture.thenAccept(this::checkReplies);

                futures.add(stopWorkerFuture);
            }
        }

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.RECEIVER) {
                CompletableFuture<List<? extends MaestroNote>> stopWorkerFuture = getMaestro()
                        .stopWorker(peerEndpoint.getDestination());

                stopWorkerFuture.thenAccept(this::checkReplies);
                futures.add(stopWorkerFuture);
            }
        }

        CompletableFuture<List<? extends MaestroNote>> stopWorkerFuture = getMaestro()
                .stopWorker(MaestroTopics.peerTopic(Role.INSPECTOR));
        stopWorkerFuture.thenAccept(this::checkReplies);

        futures.add(stopWorkerFuture);

        futures.forEach(this::verifyStopCommand);
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

            if (isIgnored(testFailedNotification)) {
                return false;
            }

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
