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

package net.orpiske.mpt.test.rate;

import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.reports.ReportsDownloader;
import net.orpiske.mpt.test.AbstractTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test executor that uses fixed rates
 */
public class FixedRateTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestExecutor.class);

    private final FixedRateTestProfile testProfile;

    private long repeat = 10000;
    private long coolDownPeriod = 10000;
    private final FixedRateTestProcessor testProcessor;

    public FixedRateTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                 final FixedRateTestProfile testProfile) throws DurationParseException {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new FixedRateTestProcessor(testProfile, reportsDownloader);

        long replyRetries = this.testProfile.getDuration().getNumericDuration();
        repeat = (replyRetries * 2);
    }

    public boolean run() {
        logger.info("Starting the test");
        try {
            // Clean up the topic
            getMaestro().collect();

            int numPeers = getNumPeers();

            getReportsDownloader().setTestNum(testProfile.getTestExecutionNumber());

            testProfile.apply(getMaestro());
            testProcessor.resetNotifications();

            startServices();
            processReplies(testProcessor, repeat, numPeers);

            if (testProcessor.isSuccessful()) {
                logger.info("Test completed successfully");
                return true;
            }

            logger.info("Test completed unsuccessfully");
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }

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
