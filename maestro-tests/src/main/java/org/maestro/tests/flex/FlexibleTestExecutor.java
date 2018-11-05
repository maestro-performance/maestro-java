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

package org.maestro.tests.flex;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerSet;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.Test;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.xunit.XUnitGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A simple test executor that can be extended for use with 3rd party testing tools
 */
public abstract class FlexibleTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final Maestro maestro;

    private final AbstractTestProfile testProfile;
    private final DistributionStrategy distributionStrategy;

    /**
     * Constructor
     * @param maestro a Maestro client instance
     * @param testProfile the test profile in use for the test
     * @param distributionStrategy the distribution strategy to use
     */
    public FlexibleTestExecutor(final Maestro maestro, final AbstractTestProfile testProfile, final DistributionStrategy distributionStrategy)
    {
        super(maestro);

        this.maestro = maestro;
        this.testProfile = testProfile;
        this.distributionStrategy = distributionStrategy;
    }


    /**
     * These two methods are NO-OP in this case because there are no multiple iterations,
     * therefore cool down period is not required/used
     */
    public long getCoolDownPeriod() {
        return 0;
    }

    public void setCoolDownPeriod(long period) {
        // NO-OP
    }

    abstract public void startServices();

    private long getTimeout() {
        return testProfile.getEstimatedCompletionTime() + 10;
    }

    private String phaseName() {
        return "run";
    }

    /**
     * Test execution logic
     * @return true if the test was successful or false otherwise
     */
    public boolean run(final Test test) {
        try {
            PeerSet peerSet = maestro.getPeers();

            testStart(test);

            logger.info("Applying the test profile");
            testProfile.apply(maestro, distributionStrategy);

            logger.info("Starting the services");
            startServices();

            logger.info("Processing the replies");
            long timeout = getTimeout();
            logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);

            List<? extends MaestroNote> results = getMaestro()
                    .waitForNotifications((int) peerSet.count(Role.AGENT))
                    .get(timeout, TimeUnit.SECONDS);

            XUnitGenerator.generate(test, results, 0);

            logger.info("Processing the notifications");
            long failed = results.stream()
                    .filter(this::isTestFailed)
                    .count();

            if (failed > 0) {
                logger.info("Test {} completed unsuccessfully", phaseName());
                return false;
            }

            logger.info("Test {} completed successfully", phaseName());
            return true;
        } catch (InterruptedException e) {
            logger.info("Test execution interrupted");
        }
        catch (TimeoutException te) {
            logger.warn("Timed out waiting for the test notifications");
        }
        catch (Exception e) {
            logger.info("Error executing the test: {}", e.getMessage(), e);
        } finally {
            distributionStrategy.reset();

            testStop();

            stopServices();
        }

        return false;
    }
}
