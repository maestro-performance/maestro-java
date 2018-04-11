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
import org.maestro.common.duration.TestDurationBuilder

maestroURL = System.getenv("MAESTRO_BROKER")
brokerURL = System.getenv("BROKER_URL")

LogConfigurator.verbose()

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

ReportsDownloader reportsDownloader = new ReportsDownloader(args[0])

IncrementalTestProfile testProfile = new SimpleTestProfile()

testProfile.setBrokerURL(brokerURL)

rate = System.getenv("RATE")
testProfile.setInitialRate(Integer.parseInt(rate))

duration = System.getenv("TEST_DURATION")
testProfile.setDuration(TestDurationBuilder.build(duration))

messageSize = System.getenv("MESSAGE_SIZE")
testProfile.setMessageSize(messageSize)

maxLatency = System.getenv("MAXIMUM_LATENCY")
testProfile.setMaximumLatency(Integer.parseInt(maxLatency))

ceilingRate = System.getenv("CEILING_RATE")
testProfile.setCeilingRate(Integer.parseInt(ceilingRate))

parallelCountIncrement = System.getenv("PARALLEL_COUNT_INCREMENT")
testProfile.setParallelCountIncrement(Integer.parseInt(parallelCountIncrement))

rateIncrement = System.getenv("RATE_INCREMENT")
testProfile.setRateIncrement(Integer.parseInt(rateIncrement))

parallelCount = System.getenv("PARALLEL_COUNT")
testProfile.setCeilingParallelCount(Integer.parseInt(parallelCount))

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile)

if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)


