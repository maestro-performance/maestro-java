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
import org.maestro.client.notes.GetResponse;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.maestro.tests.DownloadProcessor;
import org.maestro.tests.callbacks.DownloadCallback;
import org.maestro.tests.callbacks.StatsCallBack;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * A test executor that uses fixed rates
 */
public class FixedRateTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final FixedRateTestProfile testProfile;

    private static final long coolDownPeriod;
    private final DownloadProcessor downloadProcessor;

    private volatile boolean warmUp = false;

    private ScheduledExecutorService executorService;

    static {
        coolDownPeriod = config.getLong("test.fixedrate.cooldown.period", 1) * 1000;
    }

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;

        List<MaestroNoteCallback> callbackList = getMaestro().getCollector().getCallbacks();
        callbackList.add(new StatsCallBack(this));

        downloadProcessor = new DownloadProcessor(reportsDownloader);
        callbackList.add(new DownloadCallback(this, downloadProcessor));
    }

    private void reset() {
        warmUp = false;
    }

    private boolean runTest(int number, final Consumer<Maestro> apply) {
        try {
            // Clean up the topic
            getMaestro().collect();

            int numPeers = peerCount(testProfile);
            if (numPeers == 0) {
                logger.error("There are not enough peers to run the test");

                return false;
            }

            List<? extends MaestroNote> dataServers = getMaestro().getDataServer().get();
            dataServers.stream()
                    .filter(note -> note instanceof GetResponse)
                    .forEach(note -> downloadProcessor.addDataServer((GetResponse) note));

            getReportsDownloader().getOrganizer().getTracker().setCurrentTest(number);
            apply.accept(getMaestro());

            if (testProfile.getInspectorName() != null) {
                startServices(testProfile.getInspectorName());
            }
            else {
                startServices();
            }
            testStart();

            executorService = Executors.newSingleThreadScheduledExecutor();

            Runnable task = () -> { getMaestro().statsRequest(); };
            executorService.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

            long timeout = getTimeout();
            logger.info("The test {} has started and will timeout after {} seconds", phaseName(), timeout);
            List<? extends MaestroNote> results = getMaestro()
                    .waitForNotifications(timeout, numPeers)
                    .get();

            long failed = results.stream()
                    .filter(note -> isTestFailed(note))
                    .count();

            if (failed > 0) {
                logger.info("Test {} completed unsuccessfully", phaseName());
                return false;
            }

            logger.info("Test {} completed successfully", phaseName());
            return true;
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
        finally {
            try {
                final List<? extends MaestroNote> drainReplies = getMaestro().waitForDrain(15000).get();

                drainReplies.stream()
                        .filter(note -> isFailed(note));

            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error checking the draining status: {}", e.getMessage(), e);
            }

            reset();

            testStop();

            stopServices();
        }

        return false;
    }


    private String phaseName() {
        return warmUp ? "warm-up" : "run";
    }


    private long getTimeout() {
        long repeat;

        if (warmUp) {
            repeat = testProfile.getWarmUpEstimatedCompletionTime();
        }
        else {
            repeat = testProfile.getEstimatedCompletionTime();
        }

        return repeat + 10;
    }


    public boolean run() {
        logger.info("Starting the warm up execution");

        warmUp = true;
        if (runTest(0, testProfile::warmUp)) {
            try {
                Thread.sleep(getCoolDownPeriod());
                logger.info("Starting the test");

                warmUp = false;
                return runTest(1, testProfile::apply);
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

    public boolean isWarmUp() {
        return warmUp;
    }

    public FixedRateTestProfile getTestProfile() {
        return testProfile;
    }
}
