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
import org.maestro.common.LogConfigurator
import org.maestro.common.Role
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.reports.downloaders.DownloaderBuilder
import org.maestro.reports.downloaders.ReportsDownloader
import org.maestro.tests.cluster.DistributionStrategyFactory
import org.maestro.tests.rate.FixedRateTestExecutor
import org.maestro.tests.rate.FixedRateTestProfile
import org.maestro.tests.support.DefaultTestEndpoint
import org.maestro.tests.support.TestEndpointResolver
import org.maestro.tests.utils.ManagementInterface

maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker was not given"

    System.exit(1)
}

senderURL = System.getenv("SEND_URL")
if (senderURL == null) {
    println "Error: the sender point URL was not given"

    System.exit(1)
}

receiverURL = System.getenv("RECEIVE_URL")
if (receiverURL == null) {
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

maxLatency = System.getenv("MAXIMUM_LATENCY")

managementInterface = System.getenv("MANAGEMENT_INTERFACE")
inspectorName = System.getenv("INSPECTOR_NAME")
downloaderName = System.getenv("DOWNLOADER_NAME")

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

distributionStrategy = DistributionStrategyFactory.createStrategy(System.getenv("DISTRIBUTION_STRATEGY"), maestro)

ReportsDownloader reportsDownloader = DownloaderBuilder.build(downloaderName, maestro, args[0])

FixedRateTestProfile testProfile = new FixedRateTestProfile()

TestEndpointResolver endpointResolver = TestEndpointResolverFactory.createTestEndpointResolver(System.getenv("ENDPOINT_RESOLVER_NAME"))

endpointResolver.register(Role.SENDER, new DefaultTestEndpoint(sendURL))
endpointResolver.register(Role.RECEIVER, new DefaultTestEndpoint(receiveURL))

testProfile.setTestEndpointResolver(endpointResolver)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(messageSize)

if (maxLatency != null) {
    testProfile.setMaximumLatency(Integer.parseInt(maxLatency))
}

testProfile.setRate(Integer.parseInt(rate))
testProfile.setParallelCount(Integer.parseInt(parallelCount))

ManagementInterface.setupInterface(managementInterface, inspectorName, testProfile)

FixedRateTestExecutor testExecutor = new FixedRateTestExecutor(maestro, reportsDownloader, testProfile,
        distributionStrategy)

if (!testExecutor.run()) {
    maestro.stop()

    System.exit(1)
}

maestro.stop()
System.exit(0)
