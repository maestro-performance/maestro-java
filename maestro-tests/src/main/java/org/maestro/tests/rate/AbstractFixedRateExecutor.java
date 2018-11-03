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
import org.maestro.common.client.notes.Test;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.xunit.XUnitGenerator;
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

    AbstractFixedRateExecutor(final Maestro maestro,
                              final FixedRateTestProfile testProfile, final DistributionStrategy distributionStrategy) {
        super(maestro);

        this.testProfile = testProfile;

        this.distributionStrategy = distributionStrategy;
    }

    protected abstract void reset();

    protected abstract void onInit();

    protected abstract void onComplete();

    protected boolean runTest(final Test test, final BiConsumer<Maestro, DistributionStrategy> apply) {
        try {
            // Clean up the topic
            getMaestro().clear();

            PeerSet peerSet = distributionStrategy.distribute(getMaestro().getPeers());
            long numPeers = peerSet.workers();

            apply.accept(getMaestro(), distributionStrategy);

            try {
                testStart(test);

                startServices(testProfile, distributionStrategy);

                onInit();

                long timeout = getTimeout();
                logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);
                List<? extends MaestroNote> results = getMaestro()
                        .waitForNotifications((int) numPeers)
                        .get(timeout, TimeUnit.SECONDS);

                XUnitGenerator.generate(test, results, 0);

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
                onComplete();

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

            stopServices();
        }

        return false;
    }

    public void stopServices() {
        if (!isRunning()) {
            logger.trace("Not stopping the services because the test is not running");

            return;
        }

        testStop();

        logger.info("Stopping Maestro services");
        try {
            stopServices(distributionStrategy);
        }
        catch (MaestroException e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.warn("Timed out waiting for a stop response");
            }
            else {
                logger.warn(e.getMessage(), e);
            }
        }
        finally {
            distributionStrategy.reset();
        }
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
