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

package multipoint


@GrabConfig(systemClassLoader=true)

@Grab(group='commons-cli', module='commons-cli', version='1.3.1')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='org.msgpack', module='msgpack-core', version='0.8.3')

@GrabResolver(name='Eclipse', root='https://repo.eclipse.org/content/repositories/paho-releases/')
@Grab(group='org.eclipse.paho', module='org.eclipse.paho.client.mqttv3', version='1.1.1')

@GrabResolver(name='orpiske-bintray', root='https://dl.bintray.com/orpiske/libs-release')
@Grab(group='org.maestro', module='maestro-tests', version='1.3.0-SNAPSHOT')

import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.reports.ReportsDownloader
import org.maestro.tests.MultiPointProfile
import org.maestro.tests.incremental.IncrementalTestExecutor
import org.maestro.tests.incremental.IncrementalTestProfile
import org.maestro.tests.incremental.multipoint.SimpleTestProfile
import org.maestro.common.LogConfigurator
import org.maestro.common.content.MessageSize
import org.maestro.common.duration.TestDurationBuilder

maestroURL = System.getenv("MAESTRO_BROKER")
senderBrokerURL = System.getenv("SENDER_BROKER_URL")
receiveBrokerURL = System.getenv("RECEIVER_BROKER_URL")

LogConfigurator.verbose()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(args[0])

IncrementalTestProfile testProfile = new SimpleTestProfile()

testProfile.addEndPoint(new MultiPointProfile.EndPoint("sender", MaestroTopics.SENDER_DAEMONS, senderBrokerURL))
testProfile.addEndPoint(new MultiPointProfile.EndPoint("receiver", MaestroTopics.RECEIVER_DAEMONS, receiveBrokerURL))

testProfile.setInitialRate(500)
testProfile.setCeilingRate(600)

testProfile.setRateIncrement(100)

testProfile.setInitialParallelCount(2)
testProfile.setCeilingParallelCount(2)

testProfile.setDuration(TestDurationBuilder.build("120s"))
testProfile.setMessageSize(MessageSize.variable(256))
testProfile.setMaximumLatency(200)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)


