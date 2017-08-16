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
import net.orpiske.mpt.maestro.client.MaestroNoteProcessor;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.PingResponse;
import net.orpiske.mpt.maestro.notes.TestFailedNotification;
import net.orpiske.mpt.maestro.notes.TestSuccessfulNotification;
import net.orpiske.mpt.reports.ReportsDownloader;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class IncrementalTestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestExecutor.class);

    private Maestro maestro;
    private IncrementalTestProfile testProfile;

    private boolean failed = false;
    private int notifications = 0;
    private int execNum = 0;

    private ReportsDownloader reportsDownloader;

    public IncrementalTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader,
                                   final IncrementalTestProfile testProfile)
    {
        this.maestro = maestro;
        this.reportsDownloader = reportsDownloader;
        this.testProfile = testProfile;
    }

    class IterativeTestProcessor extends MaestroNoteProcessor {
        @Override
        protected void processPingResponse(PingResponse note) {
            logger.info("Elapsed time from {}: {} ms", note.getName(), note.getElapsed());
        }


        @Override
        protected void processNotifySuccess(TestSuccessfulNotification note) {
            logger.info("Test successful on {} after {} executions", note.getName(), execNum);
            logger.info("Test parameters used: " + testProfile.toString());

            String type = note.getName().split("@")[0];
            String host = note.getName().split("@")[1];

            reportsDownloader.setReportTypeDir("success");
            reportsDownloader.downloadLastSuccessful(type, host, note.getName());

            notifications++;
        }

        @Override
        protected void processNotifyFail(TestFailedNotification note) {
            logger.info("Test failed on {} after {} executions", note.getName(), execNum);
            logger.info("Test parameter used");

            String type = note.getName().split("@")[0];
            String host = note.getName().split("@")[1];

            reportsDownloader.setReportTypeDir("failed");
            reportsDownloader.downloadLastFailed(type, host, note.getName());

            failed = true;
            notifications++;
        }
    }

    private void processReplies(int numPeers) {
        long repeat = this.testProfile.getDuration();

        while (notifications != numPeers) {
            List<MaestroNote> replies = maestro.collect(1000, 1);

            (new IterativeTestProcessor()).process(replies);
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
        notifications = 0;

        maestro.startReceiver();
        maestro.startInspector();
        maestro.startSender();
    }

    public void run(String brokerURL) {
        try {
            // Clean up the topic
            maestro.collect();

            while (!failed) {
                int numPeers = getNumPeers();

                reportsDownloader.setTestNum(execNum);
                reportsDownloader.setParallelCount(testProfile.getParallelCount());

                testProfile.apply(maestro);
                startServices();
                processReplies(numPeers);

                execNum++;
                testProfile.increment();

                logger.info("Sleeping for 10 seconds to let the broker catch up");
                Thread.sleep(10000);
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }
}
