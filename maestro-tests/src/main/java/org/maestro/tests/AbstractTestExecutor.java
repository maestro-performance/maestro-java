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
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Role;
import org.maestro.common.client.exceptions.NotEnoughRepliesException;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.WorkerStartOptions;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.maestro.client.Maestro.exec;

/**
 * A simple test executor that should be extensible for most usages
 */
public abstract class AbstractTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final Maestro maestro;
    private final ReportsDownloader reportsDownloader;

    private volatile boolean running = false;
    private Instant startTime;

    protected AbstractTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader) {
        this.maestro = maestro;
        this.reportsDownloader = reportsDownloader;

        logger.trace("Creating an abstract test executor");
    }

    protected Maestro getMaestro() {
        return maestro;
    }

    protected ReportsDownloader getReportsDownloader() {
        return reportsDownloader;
    }

    protected void testStart() {
        running = true;
        startTime = Instant.now();
    }

    protected void testStop() {
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
                    exec(maestro::startWorker, destination, new WorkerStartOptions(senderName));
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

    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected final void stopServices(final DistributionStrategy distributionStrategy) throws MaestroConnectionException {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.SENDER) {
                exec(maestro::stopWorker, peerEndpoint.getDestination());
            }
        }

        for (PeerEndpoint peerEndpoint : endpoints) {
            if (peerEndpoint.getRole() == Role.RECEIVER) {
                exec(maestro::stopWorker, peerEndpoint.getDestination());
            }
        }

        try {
            exec(maestro::stopInspector, MaestroTopics.peerTopic(Role.INSPECTOR));
        }
        catch (NotEnoughRepliesException e) {
            logger.warn("While stopping the inspector: {}", e.getMessage());
        }
    }


    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    final public void stopServices() throws MaestroConnectionException {
        try {
            exec(maestro::stopWorker, MaestroTopics.WORKERS_TOPIC);
        }
        catch (NotEnoughRepliesException e) {
            logger.warn("While stopping the peers: {}", e.getMessage());
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
            TestFailedNotification testFailedNotification = (TestFailedNotification) note;
            logger.error("Test failed on {}: {}", testFailedNotification.getPeerInfo().prettyName(), testFailedNotification.getMessage());
            return true;
        }

        return false;
    }

    protected boolean isFailed(MaestroNote note) {
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

    protected void drain() {
        long drainDeadline = config.getLong("client.drain.deadline.secs", 60);

        try {
            final List<? extends MaestroNote> drainReplies = getMaestro()
                    .waitForDrain()
                    .get(drainDeadline, TimeUnit.SECONDS);

            if (drainReplies.size() == 0) {
                logger.warn("None of the peers reported a successful drain from the SUT within {} seconds",
                        drainDeadline);
            }

            drainReplies.forEach(this::isFailed);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error checking the draining status: {}", e.getMessage(), e);
        } catch (TimeoutException e) {
            logger.warn("Did not receive a drain response within {} seconds", drainDeadline);
        }
    }
}
