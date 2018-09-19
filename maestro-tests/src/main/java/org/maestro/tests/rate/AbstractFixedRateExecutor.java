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

package org.maestro.tests.rate;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.DownloadProcessor;
import org.maestro.tests.callbacks.LogRequesterCallback;
import org.maestro.tests.cluster.DistributionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public abstract class AbstractFixedRateExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final FixedRateTestProfile testProfile;
    private final DistributionStrategy distributionStrategy;

    private static final long coolDownPeriod;

    static {
        coolDownPeriod = config.getLong("test.fixedrate.cooldown.period", 1) * 1000;
    }

    AbstractFixedRateExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                              final FixedRateTestProfile testProfile, final DistributionStrategy distributionStrategy) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;

        DownloadProcessor downloadProcessor = new DownloadProcessor(reportsDownloader);
        getMaestro().getCollector().addCallback(new LogRequesterCallback(this, downloadProcessor));

        this.distributionStrategy = distributionStrategy;
    }

    protected abstract void reset();

    protected boolean runTest(int number, final BiConsumer<Maestro, DistributionStrategy> apply) {
        try {
            // Clean up the topic
            getMaestro().clear();

            PeerSet peerSet = distributionStrategy.distribute(getMaestro().getPeers());
            long numPeers = peerSet.workers();

            getReportsDownloader().getOrganizer().getTracker().setCurrentTest(number);
            apply.accept(getMaestro(), distributionStrategy);

            try {
                startServices(testProfile, distributionStrategy);

                testStart();

                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

                Runnable task = () -> getMaestro().statsRequest(MaestroTopics.WORKERS_TOPIC);
                executorService.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

                long timeout = getTimeout();
                logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);
                List<? extends MaestroNote> results = getMaestro()
                        .waitForNotifications((int) numPeers)
                        .get(timeout, TimeUnit.SECONDS);

                long failed = results.stream()
                        .filter(this::isTestFailed)
                        .count();

                if (failed > 0) {
                    logger.info("Test {} completed unsuccessfully", phaseName());
                    return false;
                }

                logger.info("Test {} completed successfully", phaseName());
                return true;
            }
            finally {
                drain();
            }
        }
        catch (TimeoutException te) {
            logger.warn("Timed out waiting for the test notifications");
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
        finally {
            reset();

            testStop();

            try {
                stopServices(distributionStrategy);
            }
            catch (MaestroException e) {
                if (e.getCause() instanceof TimeoutException) {
                    logger.warn("Timed out waiting for a stop response");
                }
                else {
                    logger.warn(e.getMessage());
                }
            }
            finally {
                distributionStrategy.reset();
            }
        }

        return false;
    }

    protected abstract String phaseName();

    protected abstract long getTimeout();

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }


    public FixedRateTestProfile getTestProfile() {
        return testProfile;
    }
}
