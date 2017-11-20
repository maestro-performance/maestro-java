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
@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='net.orpiske', module='maestro-client', version='1.2.0-SNAPSHOT')
@Grab(group='net.orpiske', module='maestro-client', version='1.2.0-SNAPSHOT')

import net.orpiske.mpt.maestro.Maestro
import net.orpiske.mpt.maestro.client.MaestroNoteProcessor
import net.orpiske.mpt.maestro.notes.MaestroNote
import net.orpiske.mpt.maestro.notes.PingResponse
import net.orpiske.mpt.maestro.notes.TestFailedNotification
import net.orpiske.mpt.maestro.notes.TestSuccessfulNotification

class ShortTestExecutor {
    private Maestro maestro;
    private int rate = 300;
    private int parallelCount = 1;
    private final maximumLatency = 500;


    ShortTestExecutor(Maestro maestro) {
        this.maestro = maestro;
    }

    class ShortTestProcessor extends MaestroNoteProcessor {
        @Override
        protected void processPingResponse(PingResponse note) {
            println  "Elapsed time from " + note.getName() + ": " + note.getElapsed() + " ms"
        }

        @Override
        protected void processNotifySuccess(TestSuccessfulNotification note) {
            println "Test successful on " + note.getName()
        }

        @Override
        protected void processNotifyFail(TestFailedNotification note) {
            println "Test failed on " + note.getName()
        }
    }


    private boolean processReplies() {
        println "Collecting replies "
        List<MaestroNote> replies = maestro.collect(1000, 10)

        (new ShortTestProcessor()).process(replies)
    }

    private void setTestParameters(String brokerURL) {
        println "Sending ping request"
        maestro.pingRequest()

        println "Setting broker"
        maestro.setBroker(brokerURL)

        println "Setting rate"
        maestro.setRate(rate);

        println "Setting parallel count"
        maestro.setParallelCount(parallelCount)

        println "Setting duration"
        maestro.setDuration("30")

        println "Setting fail-condition-latency"
        maestro.setFCL(maximumLatency)

        // Variable message size
        maestro.setMessageSize("~100")
    }

    private void startServices() {
        maestro.startReceiver()
        maestro.startInspector()
        maestro.startSender()
    }

    void run(String brokerURL) {
        setTestParameters(brokerURL)
        startServices()
        processReplies()

        println "Waiting a while for the tests to kick off"
        Thread.sleep(21000)

        processReplies()
    }
}

maestroURL = System.getenv("MAESTRO_BROKER")
brokerURL = System.getenv("BROKER_URL")

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ShortTestExecutor executor = new ShortTestExecutor(maestro)
executor.run(brokerURL);
maestro.stop()


