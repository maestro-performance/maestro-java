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
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.NodeUtils;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public AbstractTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader) {
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
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected void startServices() throws MaestroConnectionException {
        maestro.startReceiver();
        maestro.startSender();
    }

    /**
     * Start connected peers
     * @param testProfile the test profile in use
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected void startServices(final AbstractTestProfile testProfile) throws MaestroConnectionException {
        final String inspectorName = testProfile.getInspectorName();
        if (inspectorName != null) {
            maestro.startInspector(inspectorName);
        }

        maestro.startReceiver();
        maestro.startSender();
    }

    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    final public void stopServices() throws MaestroConnectionException {
        maestro.stopSender();

        /**
         * This is based on a discussion I had with Qpid Dispatch Router developers: if the
         * receiver disconnects before the sender, some messages from the sender will be
         * inflight on the router, but since the receiver will be shutting down they may not
         * be acknowledged. This causes their delivery status to be unknown on the router and
         * the disposition of this messages is then sent back to the sender as
         * MODIFIED/undeliberable. This may cause JMSExceptions to be thrown by the sender.
         *
         * By forcing a small delay between sender/router shutdown, it tries to reduce the
         * occurrences of unknown inflight messages.
         *
         * Ref: https://github.com/maestro-performance/maestro-java/issues/96
         *
         */
        final int inFlightDelay = config.getInt("executor.inflight.delay", 250);
        try {
            Thread.sleep(inFlightDelay);
        } catch (InterruptedException e) {

        }
        maestro.stopReceiver();
        maestro.stopInspector();
    }

    /**
     * Try to guess the number of connected peers
     * @param types A variable argument list of peer types to count
     * @return the number of connected peers (best guess)
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     * @throws InterruptedException if interrupted
     */
    protected int getNumPeers(String ...types) throws MaestroConnectionException, InterruptedException {
        logger.debug("Collecting responses to ensure topic is clean prior to pinging nodes");
        maestro.clean();

        logger.debug("Sending ping request");
        CompletableFuture<List<? extends MaestroNote>> repliesFuture = maestro.pingRequest();

        List<? extends MaestroNote> replies = null;
        try {
            replies = repliesFuture.get();
        } catch (ExecutionException e) {
            throw new MaestroException("Unable to collect peers", e);
        }
        Set<String> knownPeers = new LinkedHashSet<>(replies.size());

        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                if (types != null) {
                    for (String type : types) {
                        String nodeType = NodeUtils.getTypeFromName(((PingResponse) note).getName());
                        if (type.equals(nodeType)) {
                            String name = ((PingResponse) note).getName();
                            logger.debug("Accounting peer: {}/{}", name, ((PingResponse) note).getId());
                            knownPeers.add(((PingResponse) note).getId());
                        }
                    }
                }
            }
        }

        logger.info("Known peers recorded: {}", knownPeers.size());
        return knownPeers.size();
    }


    public int peerCount(final AbstractTestProfile testProfile) throws InterruptedException {
        if (testProfile.getManagementInterface() != null) {
            return getNumPeers("sender", "receiver", "inspector");
        }
        else {
            return getNumPeers("sender", "receiver");
        }
    }


    protected boolean isTestFailed(final MaestroNote note) {
        if (note instanceof TestFailedNotification) {
            TestFailedNotification testFailedNotification = (TestFailedNotification) note;
            logger.error("Test failed on {}: {}", testFailedNotification.getName(), testFailedNotification.getMessage());
            return true;
        }

        return false;
    }

    protected boolean isFailed(MaestroNote note) {
        boolean success = true;

        if (note instanceof DrainCompleteNotification) {
            success = ((DrainCompleteNotification) note).isSuccessful();
            if (!success) {
                logger.error("Drained failed for {}", ((DrainCompleteNotification) note).getName());
            }
        }

        return success;
    }


    protected long getTimeout(final AbstractTestProfile testProfile) {
        return testProfile.getEstimatedCompletionTime() + CompletionTime.getDeadline();
    }
}
