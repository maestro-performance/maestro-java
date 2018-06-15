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
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.StatsResponse;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A test executor that uses fixed rates
 */
public class FixedRateTestExecutor extends AbstractTestExecutor {
    private static final class StatsCallBack implements MaestroNoteCallback {
        private static final Logger logger = LoggerFactory.getLogger(StatsCallBack.class);

        FixedRateTestExecutor executor;

        StatsCallBack(FixedRateTestExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void call(MaestroNote note) {
            if (note instanceof StatsResponse) {
                StatsResponse statsResponse = (StatsResponse) note;
                if (statsResponse.getRate() < 200.0 && statsResponse.getRate() > 0) {
                    logger.error("The rate {} is too low. The test must be aborted",
                            statsResponse.getRate());
                }
            }
        }
    }

    private static final class TestNotificationCallBack implements MaestroNoteCallback {
        private static final Logger logger = LoggerFactory.getLogger(TestNotificationCallBack.class);

        FixedRateTestExecutor executor;


        public TestNotificationCallBack(FixedRateTestExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void call(MaestroNote note) {
            if (!executor.running) {
                return;
            }

            if (note instanceof TestSuccessfulNotification) {
                executor.successNotifications++;
                executor.testProcessor.processNotifySuccess((TestSuccessfulNotification) note);
            }
            else {
                if (note instanceof TestFailedNotification) {
                    executor.failedNotifications++;
                    executor.testProcessor.processNotifyFail((TestFailedNotification) note);
                }
            }

            int totalNotifications = executor.failedNotifications + executor.successNotifications;
            if (executor.numPeers > 0 && totalNotifications > 0) {
                if (totalNotifications >= executor.numPeers) {
                    logger.warn("Received the required amount of notifications");
                    executor.running = false;
                }
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);

    private final FixedRateTestProfile testProfile;

    private long coolDownPeriod = 10000;
    private final FixedRateTestProcessor testProcessor;

    private int numPeers = 0;
    private int successNotifications = 0;
    private int failedNotifications = 0;
    private boolean running = false;

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new FixedRateTestProcessor(testProfile, reportsDownloader);

        List<MaestroNoteCallback> callbackList = getMaestro().getCollector().getCallbacks();
        callbackList.add(new StatsCallBack(this));
        callbackList.add(new TestNotificationCallBack(this));
    }

    private boolean runTest(boolean warmUp) {
        try {
            // Clean up the topic
            getMaestro().collect();

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
            running = true;

            long repeat;

            if (warmUp) {
                repeat = testProfile.getWarmUpEstimatedCompletionTime();
            }
            else {
                repeat = testProfile.getEstimatedCompletionTime();
            }


            long i = repeat;
            while (running) {
                getMaestro().statsRequest();
                Thread.sleep(1000);
                i--;
                if (i == 0) {
                    break;
                }
            }

            if (failedNotifications > 0) {
                logger.info("Test {} completed unsuccessfully", (warmUp ? "warm-up" : ""));
            }
            else {
                logger.info("Test {} completed successfully", (warmUp ? "warm-up" : ""));
                return true;
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
        finally {
            running = false;
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
