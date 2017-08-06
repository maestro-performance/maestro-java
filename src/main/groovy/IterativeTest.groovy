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


import net.orpiske.mpt.maestro.Maestro
import net.orpiske.mpt.maestro.client.MaestroNoteProcessor
import net.orpiske.mpt.maestro.notes.MaestroNote
import net.orpiske.mpt.maestro.notes.PingResponse
import net.orpiske.mpt.maestro.notes.TestFailedNotification
import net.orpiske.mpt.maestro.notes.TestSuccessfulNotification
import net.orpiske.mpt.reports.ReportGenerator
import net.orpiske.mpt.reports.ReportsDownloader
import net.orpiske.mpt.utils.LogConfigurator

@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

class IterativeTestExecutor {
    private Maestro maestro;
    private int rate = 350;
    private int parallelCount = 1;
    private final maximumLatency = 300;
    private boolean failed = false;
    private int notifications = 0
    private int execNum = 0;

    private final String DURATION_STR = "30s"

    private ReportsDownloader reportsDownloader = new ReportsDownloader("/tmp/mpt/groovy/");


    IterativeTestExecutor(Maestro maestro) {
        this.maestro = maestro

        LogConfigurator.verbose()
    }

    class IterativeTestProcessor extends MaestroNoteProcessor {
        @Override
        protected void processPingResponse(PingResponse note) {
            println  "Elapsed time from " + note.getName() + ": " + note.getElapsed() + " ms"
        }


        @Override
        protected void processNotifySuccess(TestSuccessfulNotification note) {
            println "Test successful on " + note.getName() + " after " + execNum + " executions"
            println "Test parameters used"
            println "Rate: " + rate
            println "Parallel count: " + parallelCount
            println "Maximum latency: " + maximumLatency


            String type = note.getName().split("@")[0]
            String host = note.getName().split("@")[1]

            reportsDownloader.setReportTypeDir("success")
            reportsDownloader.downloadLastSuccessful(type, host, note.getName());

            notifications++
        }

        @Override
        protected void processNotifyFail(TestFailedNotification note) {
            println "Test failed on " + note.getName() + " after " + execNum + " executions"
            println "Test parameters used"
            println "Rate: " + rate
            println "Parallel count: " + parallelCount
            println "Maximum latency: " + maximumLatency

            String type = note.getName().split("@")[0]
            String host = note.getName().split("@")[1]

            reportsDownloader.setReportTypeDir("failed")
            reportsDownloader.downloadLastFailed(type, host, note.getName());

            failed = true;
            notifications++
        }
    }

    private boolean processReplies(int numPeers) {
        int repeat = 60

        println "Collecting replies and waiting for " + DURATION_STR

        while (notifications != numPeers) {
            List<MaestroNote> replies = maestro.collect(1000, 1)

            (new IterativeTestProcessor()).process(replies)
            repeat--
            print "\rEstimated time for test completion " + repeat + " secs"

            if (repeat == 0) {
                break
            }
        }
        println ""
    }

    private int getNumPeers() {
        int numPeers = 0;

        println "Sending ping request"
        maestro.pingRequest()

        Thread.sleep(5000)

        List<MaestroNote> replies = maestro.collect()
        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                numPeers++;
            }
        }

        return numPeers;
    }

    private void setTestParameters(String brokerURL) {
        println "Setting broker"
        maestro.setBroker(brokerURL)

        println "Setting rate"
        maestro.setRate(rate);

        println "Setting parallel count"
        maestro.setParallelCount(parallelCount)

        println "Setting duration"
        maestro.setDuration(DURATION_STR)

        println "Setting fail-condition-latency"
        maestro.setFCL(maximumLatency)

        // Variable message size
        maestro.setMessageSize("~256")
    }

    private void startServices() {
        notifications = 0

        maestro.startReceiver()
        maestro.startInspector()
        maestro.startSender()
    }

    void run(String brokerURL) {
        try {
            // Clean up the topic
            maestro.collect()

            while (!failed) {
                int numPeers = getNumPeers();

                reportsDownloader.setTestNum(execNum);
                reportsDownloader.setParallelCount(parallelCount);

                setTestParameters(brokerURL)
                startServices()
                processReplies(numPeers)

                execNum++;
                rate += 10

            }
        }
        catch (Exception e) {
            println "Error: " + e.getMessage();
        }
    }
}

//maestroURL = System.getenv("MAESTRO_BROKER")
//brokerURL = System.getenv("BROKER_URL")
//
//println "Connecting to " + maestroURL
//maestro = new Maestro(maestroURL)
//
//IterativeTestExecutor executor = new IterativeTestExecutor(maestro)
//executor.run(brokerURL);
//maestro.stop()


LogConfigurator.debug()
ReportGenerator.generate("/tmp/mpt/groovy")
