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

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.StatsResponse;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.client.notes.TestSuccessfulNotification;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.NodeUtils;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.duration.DurationCount;
import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A test executor that uses fixed rates
 */
public class FixedRateTestExecutor extends AbstractTestExecutor {
    private static final class StatsCallBack implements MaestroNoteCallback {
        private static final Logger logger = LoggerFactory.getLogger(StatsCallBack.class);

        private FixedRateTestExecutor executor;
        private Map<String, Long> counters = new HashMap<>();

        StatsCallBack(FixedRateTestExecutor executor) {
            this.executor = executor;
        }

        private void reset() {
            counters.clear();
        }

        @Override
        public void call(MaestroNote note) {
            if (!executor.warmUp || !executor.running) {
                return;
            }

            if (note instanceof StatsResponse) {
                StatsResponse statsResponse = (StatsResponse) note;
                logger.debug("Received stats {}", statsResponse);

                int targetRate = executor.testProfile.getRate();
                if (statsResponse.getRate() < (targetRate / 2) && statsResponse.getRate() > 0) {
                    logger.warn("The warm-up duration might expire of time instead of count because the current " +
                            "rate {} is much lower than the target rate {}", statsResponse.getRate(),
                            executor.testProfile.getRate());
                }

                updateCounters(statsResponse);

                long messageCount = counters.values().stream().mapToLong(Number::longValue).sum();
                logger.debug("Current message count: {}", messageCount);
                if (messageCount >= DurationCount.WARM_UP_COUNT) {
                    logger.info("The warm-up count has been reached: {} of {}",
                            messageCount, DurationCount.WARM_UP_COUNT);
                    this.executor.stopServices();
                    reset();
                }
                else {
                    final int maxDuration = 3;
                    Instant now = Instant.now();

                    Duration elapsed = Duration.between(now, executor.startTime);
                    if (elapsed.getSeconds() > (Duration.ofMinutes(maxDuration).getSeconds())) {
                        logger.warn("Stopping the warm-up because the maximum duration was reached");

                        this.executor.stopServices();
                        reset();
                    }
                }
            }
        }

        private void updateCounters(StatsResponse statsResponse) {
            final String name = statsResponse.getName();
            String type = NodeUtils.getTypeFromName(name);
            if (type.equals("inspector") || type.equals("agent")) {
                return;
            }

            Long nodeCount = counters.get(name);
            if (nodeCount == null) {
                nodeCount = statsResponse.getCount();
            }
            else {
                nodeCount += statsResponse.getCount();
            }
            counters.put(name, nodeCount);
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
                    logger.info("Received the required amount of notifications");
                    executor.running = false;
                }
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final FixedRateTestProfile testProfile;

    private static final long coolDownPeriod;
    private final FixedRateTestProcessor testProcessor;

    private int numPeers = 0;
    private volatile int successNotifications = 0;
    private volatile int failedNotifications = 0;
    private volatile boolean running = false;
    private Instant startTime;
    private volatile boolean warmUp = false;

    static {
        coolDownPeriod = config.getLong("test.fixedrate.cooldown.period", 1) * 1000;
    }

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new FixedRateTestProcessor(testProfile, reportsDownloader);

        List<MaestroNoteCallback> callbackList = getMaestro().getCollector().getCallbacks();
        callbackList.add(new StatsCallBack(this));
        callbackList.add(new TestNotificationCallBack(this));
    }

    private void reset() {
        numPeers = 0;
        successNotifications = 0;
        failedNotifications = 0;
        running = false;
        warmUp = false;
    }

    private boolean runTest() {
        try {
            // Clean up the topic
            getMaestro().collect();

            updatePeerCount();
            if (numPeers == 0) {
                logger.error("There are not enough peers to run the test");

                return false;
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

            if (testProfile.getInspectorName() != null) {
                startServices(testProfile.getInspectorName());
            }
            else {
                startServices();
            }
            running = true;
            startTime = Instant.now();

            long repeatCounter = getRepeat();
            while (running) {
                getMaestro().statsRequest();
                Thread.sleep(1000);
                repeatCounter--;
                if (repeatCounter == 0) {
                    break;
                }
            }

            int totalNotifications = getTotalNotifications();

            if (totalNotifications > 0) {
                if (failedNotifications > 0) {
                    logger.info("Test {} completed unsuccessfully", (warmUp ? "warm-up" : ""));
                } else {
                    logger.info("Test {} completed successfully", (warmUp ? "warm-up" : ""));
                    return true;
                }
            }
            else {
                logger.warn("Not enough notifications were received to assert the test completion status");
            }
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

    private int getTotalNotifications() throws InterruptedException {
        int totalNotifications;
        if (running) {
            int notificationRetries = 10;

            logger.info("Not enough notifications received yet. Backends might be quiescing after the test execution");
            do {
                totalNotifications = failedNotifications + successNotifications;
                if (totalNotifications < numPeers) {
                    Thread.sleep(500);
                    notificationRetries--;
                }
            } while (totalNotifications < numPeers && notificationRetries > 0);
        }
        else {
            totalNotifications = failedNotifications + successNotifications;
        }
        return totalNotifications;
    }

    private long getRepeat() {
        long repeat;

        if (warmUp) {
            repeat = testProfile.getWarmUpEstimatedCompletionTime();
        }
        else {
            repeat = testProfile.getEstimatedCompletionTime();
        }
        return repeat;
    }

    private void updatePeerCount() throws InterruptedException {
        if (testProfile.getManagementInterface() != null) {
            numPeers = getNumPeers("sender", "receiver", "inspector");
        }
        else {
            numPeers = getNumPeers("sender", "receiver");
        }
    }

    public boolean run() {
        logger.info("Starting the warm up execution");

        warmUp = true;
        if (runTest()) {
            try {
                Thread.sleep(getCoolDownPeriod());
                logger.info("Starting the test");

                warmUp = false;
                return runTest();
            } catch (InterruptedException e) {
                logger.warn("The test execution was interrupted");
            }
        }

        logger.error("Warm up execution failed");
        return false;
    }

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }
}
