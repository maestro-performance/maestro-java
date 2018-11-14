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
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.common.Monitor;
import org.maestro.common.client.notes.TestExecutionInfo;
import org.maestro.tests.callbacks.StatsCallBack;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A test executor that uses fixed rates and warms-up before the test
 */
public class FixedRateTestExecutor extends AbstractFixedRateExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);

    private final ScheduledExecutorService statsExecutor = Executors.newSingleThreadScheduledExecutor();
    private final StatsCallBack statsCallBack;
    private final Monitor<Object> monitor;

    private volatile boolean warmUp = false;

    public FixedRateTestExecutor(final Maestro maestro, final FixedRateTestProfile testProfile,
                                 final DistributionStrategy distributionStrategy) {
        super(maestro, testProfile, distributionStrategy);

        monitor = new Monitor(this);
        statsCallBack = new StatsCallBack(this, monitor);
    }

    protected void reset() {
        warmUp = false;
    }

    protected String phaseName() {
        return warmUp ? "warm-up" : "run";
    }

    protected long getTimeout() {
        long repeat;

        if (warmUp) {
            repeat = getTestProfile().getWarmUpEstimatedCompletionTime();
        }
        else {
            repeat = getTestProfile().getEstimatedCompletionTime();
        }

        return repeat + CompletionTime.getDeadline();
    }

    @Override
    protected void onInit() {
        if (warmUp) {
            logger.debug("Registering the callback");
            getMaestro().getCollector().addCallback(statsCallBack);

            Runnable task = () -> getMaestro().statsRequest(MaestroTopics.WORKERS_TOPIC);
            statsExecutor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        }
    }

    public synchronized void stopStatsCollection() {
        if (!statsExecutor.isShutdown()) {
            getMaestro().getCollector().removeCallback(statsCallBack);
            statsExecutor.shutdown();
        }
    }

    @Override
    protected void onComplete() {
        if (warmUp) {
            stopStatsCollection();
        }
    }

    @Override
    protected void onTestStarted() {
        if (warmUp) {
            logger.info("Waiting for the warm up to complete");
            try {
                monitor.doLock();
            } catch (InterruptedException e) {
                logger.trace("Interrupted while waiting for the warm-up condition");
            }

            logger.info("Warm-up completed. Stopping the test.");

            this.stopServices();
        }
    }

    @Override
    public boolean run(final TestExecutionInfo testExecutionInfo) {
        logger.info("Starting the warm up execution");

        warmUp = true;

        if (runTest(testExecutionInfo, getTestProfile()::warmUp)) {
            try {
                Thread.sleep(getCoolDownPeriod());
                logger.info("Starting the test");

                warmUp = false;

                testExecutionInfo.iterate();
                return runTest(testExecutionInfo, getTestProfile()::apply);
            } catch (InterruptedException e) {
                logger.warn("The test execution was interrupted");
            }
        }

        logger.error("Warm up execution failed");
        return false;
    }

    public boolean isWarmUp() {
        return warmUp;
    }
}
