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

import org.maestro.client.Maestro;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.cluster.DistributionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A test executor that uses fixed rates and goes straight to the test without warming up
 */
public class StraightFixedRateTestExecutor extends AbstractFixedRateExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StraightFixedRateTestExecutor.class);

    public StraightFixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                         final FixedRateTestProfile testProfile,
                                         final DistributionStrategy distributionStrategy)
    {
        super(maestro, reportsDownloader, testProfile, distributionStrategy);
    }

    protected void reset() { }

    protected String phaseName() {
        return "run";
    }

    protected long getTimeout() {
        return getTimeout(getTestProfile());
    }


    public boolean run() {
        logger.info("Starting the test execution");

        return runTest(0, getTestProfile()::apply);
    }
}
