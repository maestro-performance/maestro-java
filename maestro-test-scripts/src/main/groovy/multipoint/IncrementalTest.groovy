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

import org.maestro.client.Maestro
import org.maestro.common.Role
import org.maestro.reports.downloaders.DownloaderBuilder
import org.maestro.reports.downloaders.ReportsDownloader
import org.maestro.tests.cluster.DistributionStrategyFactory
import org.maestro.tests.incremental.IncrementalTestExecutor
import org.maestro.tests.incremental.IncrementalTestProfile
import org.maestro.common.LogConfigurator
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.tests.support.DefaultTestEndpoint
import org.maestro.tests.support.TestEndpointResolver
import org.maestro.tests.utils.ManagementInterface

maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker was not given"

    System.exit(1)
}


sendURL = System.getenv("SEND_URL")
if (sendURL == null) {
    println "Error: the send URL was not given"

    System.exit(1)
}

receiveURL = System.getenv("RECEIVE_URL")
if (receiveURL == null) {
    println "Error: the receive URL was not given"

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

rate = System.getenv("INITIAL_RATE")
if (rate == null) {
    println "Error: the test rate was not given"

    System.exit(1)
}

ceilingRate = System.getenv("CEILING_RATE")
if (ceilingRate == null) {
    println "Error: the test ceiling rate was not given"

    System.exit(1)
}

rateIncrement = System.getenv("RATE_INCREMENT")
if (rateIncrement == null) {
    println "Error: the rate increment was not given"

    System.exit(1)
}

initialParallelCount = System.getenv("INITIAL_PARALLEL_COUNT")
if (initialParallelCount == null) {
    println "Error: the test parallel count was not given"

    System.exit(1)
}

ceilingParallelCount = System.getenv("CEILING_PARALLEL_COUNT")
if (ceilingParallelCount == null) {
    println "Error: the test ceiling parallel count was not given"

    System.exit(1)
}

parallelCountIncrement = System.getenv("PARALLEL_COUNT_INCREMENT")
if (parallelCountIncrement == null) {
    println "Error: the test parallel count increment was not given"

    System.exit(1)
}

maxLatency = System.getenv("MAXIMUM_LATENCY")
if (maxLatency == null) {
    println "Error: the maximum acceptable latency (FCL) was not given"

    System.exit(1)
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

managementInterface = System.getenv("MANAGEMENT_INTERFACE")
inspectorName = System.getenv("INSPECTOR_NAME")
downloaderName = System.getenv("DOWNLOADER_NAME")

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

distributionStrategy = DistributionStrategyFactory.createStrategy(System.getenv("DISTRIBUTION_STRATEGY"), maestro)

ReportsDownloader reportsDownloader = DownloaderBuilder.build(downloaderName, maestro, args[0])

IncrementalTestProfile testProfile = new IncrementalTestProfile()

TestEndpointResolver endpointResolver = TestEndpointResolverFactory.createTestEndpointResolver(System.getenv("ENDPOINT_RESOLVER_NAME"))

endpointResolver.register(Role.SENDER, new DefaultTestEndpoint(sendURL))
endpointResolver.register(Role.RECEIVER, new DefaultTestEndpoint(receiveURL))

testProfile.setTestEndpointResolver(endpointResolver)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(messageSize)
testProfile.setMaximumLatency(Integer.parseInt(maxLatency))
testProfile.setInitialRate(Integer.parseInt(rate))
testProfile.setCeilingRate(Integer.parseInt(ceilingRate))
testProfile.setRateIncrement(Integer.parseInt(rateIncrement))
testProfile.setParallelCountIncrement(Integer.parseInt(parallelCountIncrement))
testProfile.setInitialParallelCount(Integer.parseInt(initialParallelCount))
testProfile.setCeilingParallelCount(Integer.parseInt(ceilingParallelCount))

ManagementInterface.setupInterface(managementInterface, inspectorName, testProfile)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, reportsDownloader, testProfile,
        distributionStrategy)

boolean ret = testExecutor.run()

reportsDownloader.waitForComplete()
maestro.stop()

if (!ret) {
    System.exit(1)
}

System.exit(0)