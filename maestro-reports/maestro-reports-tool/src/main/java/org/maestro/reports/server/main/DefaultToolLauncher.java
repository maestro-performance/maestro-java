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
 *
 */

package org.maestro.reports.server.main;

import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.server.DefaultReportsServer;
import org.maestro.reports.server.ReportsServer;
import org.maestro.reports.server.collector.DefaultReportsCollector;
import org.maestro.worker.common.MaestroWorkerManager;
import org.maestro.worker.common.executor.MaestroWorkerExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;

public class DefaultToolLauncher implements ReportsToolLauncher {
    private static final Logger logger = LoggerFactory.getLogger(DefaultToolLauncher.class);

    private final File dataDir;
    private final boolean offline;
    private final String maestroUrl;
    private final PeerInfo peerInfo;

    public DefaultToolLauncher(File dataDir, boolean offline, String maestroUrl, String host) {
        this.dataDir = dataDir;
        this.offline = offline;
        this.maestroUrl = maestroUrl;

        peerInfo = new ReportsServerPeer(host);
    }

    public int launchServices() {
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executors;
        Future<ReportsServer> reportsServerFuture;

        try {
            if (!offline) {
                executors = Executors.newFixedThreadPool(2);

                logger.debug("Starting the collector");
                executors.submit(() -> startCollector(latch));

                logger.debug("Starting the reports server");
                reportsServerFuture = executors.submit(() -> startServer(latch));
            }
            else {
                executors = Executors.newSingleThreadScheduledExecutor();

                logger.debug("Starting the reports server (offline)");
                reportsServerFuture = executors.submit(() -> startServer(latch));
            }

            try {
                latch.await();
                executors.shutdownNow();
            }
            catch (InterruptedException e) {
                logger.trace("Interrupted");
            }

            ReportsServer reportsServer = reportsServerFuture.get();
            reportsServer.stop();
        } catch (InterruptedException e) {
            logger.trace("Interrupted the execution of the reports tools");
        } catch (ExecutionException | MaestroException e) {
            logger.error("Error while executing the reports tool: {}", e.getMessage(), e);

            return 1;
        }

        return 0;
    }

    protected void startCollector(final CountDownLatch latch, final MaestroWorkerManager maestroPeer) {
        try {
            String[] topics = MaestroTopics.collectorTopics(maestroPeer.getId(), peerInfo);

            MaestroWorkerExecutor executor = new MaestroWorkerExecutor(maestroPeer);
            logger.info("Starting the reports Collector instance");
            executor.start(topics, 10, 1000);
            executor.run();
            logger.info("Collector worker has finished running");
        } catch (Throwable t) {
            logger.error("Unable to start the Maestro reports collector: {}", t.getMessage(), t);
        }
        finally {
            latch.countDown();
        }
    }

    protected void startCollector(final CountDownLatch latch) {
        startCollector(latch, new DefaultReportsCollector(maestroUrl, peerInfo, dataDir));
    }

    protected void startServer(final CountDownLatch latch, final ReportsServer reportsServer) {
        try {
            logger.info("Starting the reports server instance");
            reportsServer.start();

            logger.info("Started the reports server instance");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The tool launcher thread was interrupted");
        } catch (Throwable t) {
            logger.error("Unable to start the Maestro reports server: {}", t.getMessage(), t);
        }
        finally {
            latch.countDown();
        }
    }

    protected ReportsServer startServer(final CountDownLatch latch) {
        ReportsServer reportsServer = new DefaultReportsServer();

        startServer(latch, reportsServer);

        return reportsServer;
    }

    protected File getDataDir() {
        return dataDir;
    }

    protected boolean isOffline() {
        return offline;
    }

    protected String getMaestroUrl() {
        return maestroUrl;
    }

    protected PeerInfo getPeerInfo() {
        return peerInfo;
    }
}
