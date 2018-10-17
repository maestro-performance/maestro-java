/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.tests.incremental;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.Test;
import org.maestro.common.client.notes.TestDetails;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.cluster.DistributionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * An executor that runs the test ever incrementing the rate and parallel connections as defined by the profile
 */
public class IncrementalTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final IncrementalTestProfile testProfile;

    private static final long coolDownPeriod;
    private final DistributionStrategy distributionStrategy;

    static {
        coolDownPeriod = config.getLong("test.incremental.cooldown.period", 1) * 1000;
    }

    /**
     * Constructor
     * @param maestro a Maestro client instance
     * @param testProfile the test profile in use for the test
     */
    public IncrementalTestExecutor(final Maestro maestro, final IncrementalTestProfile testProfile,
                                   final DistributionStrategy distributionStrategy)
    {
        super(maestro);

        this.testProfile = testProfile;
        this.distributionStrategy = distributionStrategy;
    }

    private long getTimeout() {
        return getTimeout(testProfile);
    }

    private String phaseName() {
        return "run";
    }

    private boolean runTest(final Test test) {
        logger.info("Starting test execution {}", test.getTestIteration());

        try {
            // Clean up the topic
            getMaestro().clear();

            PeerSet peerSet = distributionStrategy.distribute(getMaestro().getPeers());
            long numPeers = peerSet.workers();

            testProfile.apply(getMaestro(), distributionStrategy);

            try {
                testStart(test);

                startServices(testProfile, distributionStrategy);

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


    public boolean run(final String scriptName, final String description, final String comments) {
        int testIteration = 0;
        boolean successful;

        final TestDetails testDetails = new TestDetails(description, comments);

        do {
            Test test;

            if (testIteration == 0) {
                test = new Test(Test.NEXT, testIteration, "incremental", scriptName, testDetails);
            }
            else {
                test = new Test(Test.LAST, Test.NEXT, "incremental", scriptName, testDetails);
            }

            successful = runTest(test);
            if (!successful) {
                break;
            }

            testProfile.increment();
            if (testProfile.isOverCeiling()) {
                break;
            }
            
            testIteration++;
        } while (true);

        if (!successful) {
            logger.error("Test execution failed");
        }

        return successful;
    }

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }
}
