package singlepoint

/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this normalizedFile except in compliance with the License.
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
@Grab(group='net.orpiske', module='maestro-common', version='1.2.5')
@Grab(group='net.orpiske', module='maestro-client', version='1.2.5')
@Grab(group='net.orpiske', module='maestro-tests', version='1.2.5')
@Grab(group='net.orpiske', module='maestro-reports', version='1.2.5')

import net.orpiske.mpt.maestro.Maestro
import net.orpiske.mpt.reports.ReportsDownloader
import net.orpiske.mpt.test.incremental.IncrementalTestExecutor
import net.orpiske.mpt.test.incremental.IncrementalTestProfile
import net.orpiske.mpt.test.singlepoint.SimpleTestProfile
import net.orpiske.mpt.common.LogConfigurator
import net.orpiske.mpt.common.content.MessageSize
import net.orpiske.mpt.common.duration.TestDurationBuilder

maestroURL = System.getenv("MAESTRO_BROKER")
brokerURL = System.getenv("BROKER_URL")
rate = System.getenv("RATE")
messageSize = System.getenv("MESSAGE_SIZE")
rateIncrement = System.getenv("RATE_INCREMENT")
ceilingRate = System.getenv("CEILING_RATE")
parallelCount = System.getenv("PARALLEL_COUNT")
pcIncrement = System.getenv("PARALLEL_COUNT_INCREMENT")
duration = System.getenv("TEST_DURATION")


LogConfigurator.verbose()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(args[0]);

IncrementalTestProfile testProfile = new SimpleTestProfile();

testProfile.setBrokerURL(brokerURL)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(MessageSize.fixed(Long.parseLong(messageSize)))

testProfile.setMaximumLatency(200)

testProfile.setInitialRate(Integer.parseInt(rate));
testProfile.setCeilingRate(Integer.parseInt(ceilingRate))
testProfile.setRateIncrement(Integer.parseInt(rateIncrement))

testProfile.setInitialParallelCount(Integer.parseInt(parallelCount))
testProfile.setCeilingParallelCount(Integer.parseInt(parallelCount))
testProfile.setParallelCountIncrement(Integer.parseInt(pcIncrement))

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)


