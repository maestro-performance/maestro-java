package singlepoint

// import java.lang.Math.*

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
@Grab(group='org.maestro', module='maestro-tests', version='1.3.0-SNAPSHOT')

import org.maestro.client.Maestro
import org.maestro.reports.ReportsDownloader
import org.maestro.tests.incremental.IncrementalTestExecutor
import org.maestro.tests.incremental.IncrementalTestProfile
import org.maestro.tests.incremental.singlepoint.SimpleTestProfile
import org.maestro.common.LogConfigurator
import org.maestro.common.content.MessageSize
import org.maestro.common.duration.TestDurationBuilder


maestroURL = System.getenv("MAESTRO_BROKER")
brokerURL = System.getenv("SEND_RECEIVE_URL")
duration = System.getenv("TEST_DURATION")
steps = Integer.parseInt(System.getenv("STEPS"))


messageSize = Long.parseLong(System.getenv("MESSAGE_SIZE"))

combinedRate = Integer.parseInt(System.getenv("COMBINED_INITIAL_RATE"))
combiniedCeilingRate = Integer.parseInt(System.getenv("COMBINED_CEILING_RATE"))

parallelCount = Integer.parseInt(System.getenv("INITIAL_PARALLEL_COUNT"))
ceilingParallelCount = Integer.parseInt(System.getenv("CEILING_PARALLEL_COUNT"))
parallelCountIncrement = Integer.parseInt(System.getenv("PARALLEL_COUNT_INCREMENT"))


rate = (combinedRate / parallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated base rate $rate"

ceilingRate = (combiniedCeilingRate / parallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated ceiling rate $ceilingRate"

rateIncrement = (ceilingRate - rate) / steps
println "Calculated rate increment $rateIncrement"

LogConfigurator.debug()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(args[0]);

IncrementalTestProfile testProfile = new SimpleTestProfile();

testProfile.setBrokerURL(brokerURL)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(MessageSize.fixed(messageSize))

maxLatencyStr = System.getenv("MAXIMUM_LATENCY");
if (maxLatencyStr != null) {
    int maxLatency = Integer.parseInt(maxLatencyStr)
    testProfile.setMaximumLatency(maxLatency)
}

testProfile.setInitialRate(rate.intValue());
testProfile.setCeilingRate(ceilingRate.intValue())
testProfile.setRateIncrement(rateIncrement.intValue())

testProfile.setInitialParallelCount(parallelCount)
testProfile.setCeilingParallelCount(ceilingParallelCount)
testProfile.setParallelCountIncrement(parallelCountIncrement)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)


