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

import org.maestro.reports.ReportsDownloader;
import org.maestro.tests.AbstractTestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncrementalTestProcessor extends AbstractTestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestProcessor.class);

    private final IncrementalTestProfile testProfile;

    /**
     * Constructor
     * @param testProfile
     * @param reportsDownloader
     */
    public IncrementalTestProcessor(IncrementalTestProfile testProfile, ReportsDownloader reportsDownloader) {
        super(testProfile, reportsDownloader);

        this.testProfile = testProfile;
    }

    public boolean isSuccessful() {
        if (testProfile.getParallelCount() >= testProfile.getCeilingParallelCount()) {
            logger.trace("Profile parallel count {} exceeds the profile ceiling count {}", testProfile.getParallelCount(),
                    testProfile.getCeilingParallelCount());

            if (testProfile.getRate() >= testProfile.getCeilingRate()) {
                logger.trace("Profile rate {} exceeds the profile ceiling rate {}", testProfile.getRate(),
                        testProfile.getCeilingRate());

                return true;
            }
        }

        return false;
    }

    public void increaseFlushWaitSeconds() {
        setFlushWaitSeconds(AbstractTestProcessor.DEFAULT_WAIT_TIME * testProfile.parallelCount);
    }
}

