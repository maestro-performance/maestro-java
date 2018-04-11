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

import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestProcessor;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test processor for fixed rate tests
 */
public class FixedRateTestProcessor extends AbstractTestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestProcessor.class);

    /**
     * Constructor
     * @param testProfile
     * @param reportsDownloader
     */
    public FixedRateTestProcessor(FixedRateTestProfile testProfile, ReportsDownloader reportsDownloader) {
        super(testProfile, reportsDownloader);

        setFlushWaitSeconds(AbstractTestProcessor.DEFAULT_WAIT_TIME * testProfile.getParallelCount());
    }

    public boolean isSuccessful() {
        return !super.isFailed();
    }
}

