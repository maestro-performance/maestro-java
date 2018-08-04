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

import org.maestro.client.Maestro;
import org.maestro.client.notes.GetResponse;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.reports.downloaders.ReportsDownloader;
import org.maestro.tests.AbstractTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An executor that runs the test ever incrementing the rate and parallel connections as defined by the profile
 */
public class IncrementalTestExecutor extends AbstractTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);

    private final IncrementalTestProfile testProfile;

    private long coolDownPeriod = 10000;
    private final IncrementalTestProcessor testProcessor;

    /**
     * Constructor
     * @param maestro a Maestro client instance
     * @param reportsDownloader the reports downloader in use for the test
     * @param testProfile the test profile in use for the test
     */
    public IncrementalTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                   final IncrementalTestProfile testProfile) {
        super(maestro, reportsDownloader);

        this.testProfile = testProfile;
        this.testProcessor = new IncrementalTestProcessor(testProfile, reportsDownloader);
    }

    public boolean run() {
        long repeat = testProfile.getEstimatedCompletionTime();

        try {
            // Clean up the topic
            getMaestro().collect();

            while (!testProcessor.isCompleted()) {
                int numPeers;
                if (testProfile.getManagementInterface() != null) {
                    numPeers = getNumPeers("sender", "receiver", "inspector");
                }
                else {
                    numPeers = getNumPeers("sender", "receiver");
                }

                List<? extends MaestroNote> dataServers = getMaestro().getDataServer().get();
                dataServers.stream()
                        .filter(note -> note instanceof GetResponse)
                        .forEach(note -> super.addDataServer((GetResponse) note, testProcessor));

                getReportsDownloader().getOrganizer().getTracker().setCurrentTest(testProfile.getTestExecutionNumber());

                testProfile.apply(getMaestro());
                testProcessor.resetNotifications();

                if (testProfile.getInspectorName() != null) {
                    startServices(testProfile.getInspectorName());
                }
                else {
                    startServices();
                }
                processNotifications(testProcessor, repeat, numPeers);

                testProfile.increment();
                if (testProfile.isOverCeiling()) {
                    break;
                }

                logger.info("Sleeping for {} milliseconds to let the broker catch up", coolDownPeriod);
                Thread.sleep(coolDownPeriod);
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

    @Override
    public long getCoolDownPeriod() {
        return coolDownPeriod;
    }

    public void setCoolDownPeriod(long period) {
        this.coolDownPeriod = period;
    }
}
