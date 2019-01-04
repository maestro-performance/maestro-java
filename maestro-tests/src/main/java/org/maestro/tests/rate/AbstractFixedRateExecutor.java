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
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.TestExecutionInfo;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.xunit.XUnitGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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

    AbstractFixedRateExecutor(final Maestro maestro,
                              final FixedRateTestProfile testProfile, final DistributionStrategy distributionStrategy) {
        super(maestro);

        this.testProfile = testProfile;

        this.distributionStrategy = distributionStrategy;
    }

    protected abstract void reset();

    protected abstract void onInit();

    protected abstract void onTestStarted();

    protected abstract void onComplete();

    protected boolean runTest(final TestExecutionInfo testExecutionInfo, final BiConsumer<Maestro, DistributionStrategy> apply) {
        try {
            // Clean up the topic
            getMaestro().clear();

            PeerSet peerSet = distributionStrategy.distribute(getMaestro().getPeers());
            long numPeers = peerSet.workers();

            apply.accept(getMaestro(), distributionStrategy);

            try {
                Instant start = Instant.now();

                CompletableFuture<List<? extends MaestroNote>> notificationsFuture =
                        doTestStart(testExecutionInfo, (int) numPeers);

                CompletableFuture<Boolean> failures = notificationsFuture.thenApply(this::onNotified);

                onTestStarted();

                final long timeout = getTimeout();
                List<? extends MaestroNote> results = notificationsFuture.get(timeout, TimeUnit.SECONDS);

                XUnitGenerator.generate(testExecutionInfo.getTest(), results, start);

                if (results.size() != numPeers) {
                    forceStop(distributionStrategy);
                }

                return failures.get();
            }
            finally {
                onComplete();

                drain(peerSet);
            }
        }
        catch (TimeoutException te) {
            logger.warn("Timed out waiting for the test notifications");
            forceStop(distributionStrategy);
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
            forceStop(distributionStrategy);
        }
        finally {
            reset();

            stopServices();
        }

        return false;
    }

    private CompletableFuture<List<? extends MaestroNote>> doTestStart(final TestExecutionInfo testExecutionInfo, int numPeers) {
        final long timeout = getTimeout();
        testStart(testExecutionInfo);

        startServices(testProfile, distributionStrategy);

        onInit();

        logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);

        return getMaestro().waitForNotifications(numPeers);
    }

    protected boolean onNotified(final List<? extends MaestroNote> results) {
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

    @Override
    public void stopServices() {
        if (!isRunning()) {
            logger.trace("Not stopping the services because the test is not running");

            return;
        }

        testStop();

        distributionStrategy.reset();
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
