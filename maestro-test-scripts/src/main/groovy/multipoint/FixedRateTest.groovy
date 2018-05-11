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

package singlepoint

@GrabConfig(systemClassLoader=true)

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-tests', version='1.3.0-SNAPSHOT')


import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.common.LogConfigurator
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.reports.InspectorReportResolver
import org.maestro.reports.InterconnectInspectorReportResolver
import org.maestro.reports.ReportsDownloader
import org.maestro.tests.MultiPointProfile
import org.maestro.tests.rate.FixedRateTestExecutor
import org.maestro.tests.rate.multipoint.FixedRateMultipointTestProfile

maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker was not given"

    System.exit(1)
}

senderBrokerURL = System.getenv("SEND_URL")
if (senderBrokerURL == null) {
    println "Error: the sender point URL was not given"

    System.exit(1)
}

receiverBrokerURL = System.getenv("RECEIVE_URL")
if (receiverBrokerURL == null) {
    println "Error: the receiver point URL was not given"

    System.exit(1)
}

messageSize = System.getenv("MESSAGE_SIZE")
if (messageSize == null) {
    println "Error: the message size was not given"

    System.exit(1)
}

duration = System.getenv("TEST_DURATION")
if (duration == null) {
    println "Error: the test duration was not given"

    System.exit(1)
}

rate = System.getenv("RATE")
if (rate == null) {
    println "Error: the test rate was not given"

    System.exit(1)
}

parallelCount = System.getenv("PARALLEL_COUNT")
if (parallelCount == null) {
    println "Error: the test parallel count was not given"

    System.exit(1)
}

managementInterface = System.getenv("MANAGEMENT_INTERFACE");
inspectorName = System.getenv("INSPECTOR_NAME");

LogConfigurator.verbose()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(args[0])

FixedRateMultipointTestProfile testProfile = new FixedRateMultipointTestProfile()

testProfile.addEndPoint(new MultiPointProfile.EndPoint("sender", MaestroTopics.SENDER_DAEMONS, senderBrokerURL))
testProfile.addEndPoint(new MultiPointProfile.EndPoint("receiver", MaestroTopics.RECEIVER_DAEMONS, receiverBrokerURL))

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(messageSize)
testProfile.setMaximumLatency(20000)
testProfile.setRate(Integer.parseInt(rate))
testProfile.setParallelCount(Integer.parseInt(parallelCount))

if (managementInterface != null) {
    if (inspectorName != null) {
        testProfile.setInspectorName(inspectorName)
        testProfile.setManagementInterface(managementInterface)

        if(inspectorName == "InterconnectInspector") {
            reportsDownloader.addReportResolver("inspector", new InterconnectInspectorReportResolver())
        }
        else {
            reportsDownloader.addReportResolver("inspector", new InspectorReportResolver())
        }
    }
    else {
        println "A management interface was provided by no inspector name was given. Ignoring ..."
    }
}
else {
    println "No management interface address was given"
}


FixedRateTestExecutor testExecutor = new FixedRateTestExecutor(maestro, reportsDownloader, testProfile)
if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)
