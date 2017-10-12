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
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.PingResponse;
import net.orpiske.mpt.reports.ReportsDownloader;
import net.orpiske.mpt.utils.DurationParseException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class IncrementalTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);

    private Maestro maestro;
    private IncrementalTestProfile testProfile;

    private long replyRetries;

    private ReportsDownloader reportsDownloader;
    private IncrementalTestProcessor testProcessor;

    public IncrementalTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                   final IncrementalTestProfile testProfile) throws DurationParseException {
        this.maestro = maestro;
        this.reportsDownloader = reportsDownloader;
        this.testProfile = testProfile;
        this.testProcessor = new IncrementalTestProcessor(testProfile, reportsDownloader);

        replyRetries = this.testProfile.getDuration().getNumericDuration();
    }


    private void processReplies(int numPeers) {
        long repeat = (replyRetries * 2);

        while (testProcessor.getNotifications() != numPeers) {
            List<MaestroNote> replies = maestro.collect(1000, 1);

            testProcessor.process(replies);
            repeat--;
            logger.debug("Estimated time for test completion: {} secs", repeat);

            if (repeat == 0) {
                break;
            }
        }
    }

    private int getNumPeers() throws MqttException, IOException, InterruptedException {
        int numPeers = 0;

        logger.debug("Collecting responses to ensure topic is clean prior to pinging nodes");
        maestro.collect();

        logger.debug("Sending ping request");
        maestro.pingRequest();

        Thread.sleep(5000);

        List<MaestroNote> replies = maestro.collect();
        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                numPeers++;
            }
        }

        return numPeers;
    }


    private void startServices() throws MqttException, IOException {
        testProcessor.resetNotifications();

        maestro.startReceiver();
        maestro.startInspector();
        maestro.startSender();
    }

    public boolean run() {
        try {
            // Clean up the topic
            maestro.collect();

            while (!testProcessor.isCompleted()) {
                int numPeers = getNumPeers();

                reportsDownloader.setTestNum(testProfile.getTestExecutionNumber());

                testProfile.apply(maestro);
                startServices();
                processReplies(numPeers);

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
