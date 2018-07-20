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
import org.maestro.client.exchange.MaestroProcessedInfo;
import org.maestro.client.notes.MaestroNotification;
import org.maestro.client.notes.PingResponse;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.NodeUtils;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple test executor that should be extensible for most usages
 */
public abstract class AbstractTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final Maestro maestro;
    private final ReportsDownloader reportsDownloader;


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
     * @param inspectorName the name of the inspector to use
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected void startServices(final String inspectorName) throws MaestroConnectionException {
        maestro.startInspector(inspectorName);
        maestro.startReceiver();
        maestro.startSender();
    }

    /**
     * Stop connected peers
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    final protected void stopServices() throws MaestroConnectionException {
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
     * @return the number of connected peers (best guess)
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     * @throws InterruptedException if interrupted
     */
    protected int getNumPeers() throws MaestroConnectionException, InterruptedException {
        int numPeers = 0;

        logger.debug("Collecting responses to ensure topic is clean prior to pinging nodes");
        maestro.collect();

        logger.debug("Sending ping request");
        maestro.pingRequest();

        Thread.sleep(5000);

        List<MaestroNote> replies = maestro.collect();
        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                numPeers++;
            }
        }

        return numPeers;
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
        maestro.collect();

        logger.debug("Sending ping request");
        maestro.pingRequest();

        Thread.sleep(5000);

        List<MaestroNote> replies = maestro.collect();
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


    /**
     * Resolve the data servers connected to the test cluster
     * @throws MaestroConnectionException if there's a connection error while communicating w/ the Maestro broker
     */
    protected void resolveDataServers() throws MaestroConnectionException {
        logger.debug("Collecting responses to ensure topic is clean prior to collecting data servers");
        maestro.collect();

        logger.debug("Sending request to collect data servers");
        maestro.getDataServer();
    }

    protected MaestroProcessedInfo processNotifications(final AbstractTestProcessor testProcessor, long repeat, int numPeers) {
        List<MaestroNote> replies = getMaestro().collect(1000, repeat, numPeers, reply -> reply instanceof MaestroNotification);

        return testProcessor.process(replies);
    }


    protected MaestroProcessedInfo processReplies(final AbstractTestProcessor testProcessor, long repeat, int numPeers) {
        List<MaestroNote> replies = getMaestro().collect(1000, repeat, numPeers);

        return testProcessor.process(replies);
    }
}
