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
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.callbacks.StatsCallBack;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test executor that uses fixed rates and warms-up before the test
 */
public class FixedRateTestExecutor extends AbstractFixedRateExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);

    private volatile boolean warmUp = false;

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile,
                                 final DistributionStrategy distributionStrategy) {
        super(maestro, reportsDownloader, testProfile, distributionStrategy);

        getMaestro().getCollector().addCallback(new StatsCallBack(this));
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


    public boolean run() {
        logger.info("Starting the warm up execution");

        warmUp = true;
        if (runTest(0, getTestProfile()::warmUp)) {
            try {
                Thread.sleep(getCoolDownPeriod());
                logger.info("Starting the test");

                warmUp = false;
                return runTest(1, getTestProfile()::apply);
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
