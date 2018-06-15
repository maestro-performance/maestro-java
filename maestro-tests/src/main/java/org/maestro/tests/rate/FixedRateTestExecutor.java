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

package org.maestro.tests.rate;

import org.maestro.client.Maestro;
import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test executor that uses fixed rates
 */
public class FixedRateTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);

    private final FixedRateTestProfile testProfile;


    private long coolDownPeriod = 10000;
    private final FixedRateTestProcessor testProcessor;

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new FixedRateTestProcessor(testProfile, reportsDownloader);


    }

    private boolean runTest(boolean warmUp) {
        try {
            // Clean up the topic
            getMaestro().collect();

            int numPeers;
            if (testProfile.getManagementInterface() != null) {
                numPeers = getNumPeers("sender", "receiver", "inspector");
            }
            else {
                numPeers = getNumPeers("sender", "receiver");
            }

            resolveDataServers();
            processReplies(testProcessor, 60, numPeers);

            if (warmUp) {
                getReportsDownloader().getOrganizer().getTracker().setCurrentTest(0);
                testProfile.warmUp(getMaestro());
            }
            else {
                getReportsDownloader().getOrganizer().getTracker().setCurrentTest(1);
                testProfile.apply(getMaestro());
            }

            testProcessor.resetNotifications();

            if (testProfile.getInspectorName() != null) {
                startServices(testProfile.getInspectorName());
            }
            else {
                startServices();
            }

            long repeat;

            if (warmUp) {
                repeat = testProfile.getWarmUpEstimatedCompletionTime();
            }
            else {
                repeat = testProfile.getEstimatedCompletionTime();
            }

            processNotifications(testProcessor, repeat, numPeers);

            if (testProcessor.isSuccessful()) {
                logger.info("Test {} completed successfully", (warmUp ? "warm-up" : ""));

                return true;
            }

            logger.info("Test {} completed unsuccessfully", (warmUp ? "warm-up" : ""));
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }

        return false;
    }

    public boolean run() {
        logger.info("Starting the warm up execution");

        if (runTest(true)) {
            logger.info("Starting the test");

            return runTest(false);
        }

        logger.error("Warm up execution failed");
        return false;
    }

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }

    @Override
    public void setCoolDownPeriod(long period) {
        this.coolDownPeriod = period;
    }
}
