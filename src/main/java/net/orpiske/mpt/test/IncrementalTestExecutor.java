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

package net.orpiske.mpt.test;

import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.reports.ReportsDownloader;
import net.orpiske.mpt.utils.DurationParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncrementalTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);

    private IncrementalTestProfile testProfile;

    private long repeat;
    private IncrementalTestProcessor testProcessor;

    public IncrementalTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                   final IncrementalTestProfile testProfile) throws DurationParseException {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new IncrementalTestProcessor(testProfile, reportsDownloader);

        long replyRetries = this.testProfile.getDuration().getNumericDuration();
        repeat = (replyRetries * 2);
    }

    public boolean run() {
        try {
            // Clean up the topic
            getMaestro().collect();

            while (!testProcessor.isCompleted()) {
                int numPeers = getNumPeers();

                getReportsDownloader().setTestNum(testProfile.getTestExecutionNumber());

                testProfile.apply(getMaestro());
                testProcessor.resetNotifications();

                startServices();
                processReplies(testProcessor, repeat, numPeers);

                testProfile.increment();

                logger.info("Sleeping for 10 seconds to let the broker catch up");
                Thread.sleep(10000);
            }

            if (testProcessor.isSuccessful()) {
                return true;
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }

        return false;
    }
}
