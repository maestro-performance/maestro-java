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
package singlepoint

import org.maestro.client.Maestro
import org.maestro.common.Role
import org.maestro.common.client.notes.TestExecutionInfo
import org.maestro.common.client.notes.TestExecutionInfoBuilder
import org.maestro.tests.cluster.DistributionStrategyFactory
import org.maestro.tests.incremental.IncrementalTestExecutor
import org.maestro.tests.incremental.IncrementalTestProfile
import org.maestro.common.LogConfigurator
import org.maestro.common.content.MessageSize
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.tests.support.DefaultTestEndpoint
import org.maestro.tests.support.TestEndpointResolver
import org.maestro.tests.support.TestEndpointResolverFactory
import org.maestro.tests.utils.ManagementInterface


maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker was not given"

    System.exit(1)
}

sendReceiveURL = System.getenv("SEND_RECEIVE_URL")
if (sendReceiveURL == null) {
    println "Error: the send/receive URL was not given"

    System.exit(1)
}

messageSizeStr = System.getenv("MESSAGE_SIZE")
if (messageSizeStr == null) {
    println "Error: the message size was not given"

    System.exit(1)
}
if (messageSizeStr.startsWith("~")) {
    println "Error: fair incremental test requires a fixed message size"

    System.exit(1)
}

messageSize = Long.parseLong(messageSizeStr)

duration = System.getenv("TEST_DURATION")
if (duration == null) {
    println "Error: the test duration was not given"

    System.exit(1)
}

combinedRateStr = System.getenv("COMBINED_INITIAL_RATE")
if (combinedRateStr == null) {
    println "Error: the combined rate was not given"

    System.exit(1)
}
combinedRate = Integer.parseInt(combinedRateStr)

combinedCeilingRateStr = System.getenv("COMBINED_CEILING_RATE")
if (combinedCeilingRateStr == null) {
    println "Error: the combined ceiling rate was not given"

    System.exit(1)
}
combinedCeilingRate = Integer.parseInt(combinedCeilingRateStr)

initialParallelCountStr = System.getenv("INITIAL_PARALLEL_COUNT")
if (initialParallelCountStr == null) {
    println "Error: the test initial parallel count was not given"

    System.exit(1)
}
initialParallelCount = Integer.parseInt(initialParallelCountStr)

ceilingParallelCountStr = System.getenv("CEILING_PARALLEL_COUNT")
if (ceilingParallelCountStr == null) {
    println "Error: the test ceiling parallel count was not given"

    System.exit(1)
}
ceilingParallelCount = Integer.parseInt(ceilingParallelCountStr)


parallelCountIncrementStr = System.getenv("PARALLEL_COUNT_INCREMENT")
if (parallelCountIncrementStr == null) {
    println "Error: the test parallel count increment was not given"

    System.exit(1)
}
parallelCountIncrement = Integer.parseInt(parallelCountIncrementStr)

stepsStr = System.getenv("STEPS")
if (stepsStr == null) {
    println "Error: the number of test steps were not given"

    System.exit(1)
}
steps = Integer.parseInt(stepsStr)

maxLatency = System.getenv("MAXIMUM_LATENCY")
if (maxLatency == null) {
    println "Error: the maximum acceptable latency (FCL) was not given"

    System.exit(1)
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

managementInterface = System.getenv("MANAGEMENT_INTERFACE")
inspectorName = System.getenv("INSPECTOR_NAME")


rate = (combinedRate / initialParallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated base rate $rate"

ceilingRate = (combinedCeilingRate / initialParallelCount ) * (1 - (Math.log10(messageSize.doubleValue())) / 10)
println "Calculated ceiling rate $ceilingRate"

rateIncrement = (ceilingRate - rate) / steps
println "Calculated rate increment $rateIncrement"


println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

distributionStrategy = DistributionStrategyFactory.createStrategy(System.getenv("DISTRIBUTION_STRATEGY"), maestro)

IncrementalTestProfile testProfile = new IncrementalTestProfile()

TestEndpointResolver endpointResolver = TestEndpointResolverFactory.createTestEndpointResolver(System.getenv("ENDPOINT_RESOLVER_NAME"))

endpointResolver.register(Role.SENDER, new DefaultTestEndpoint(sendReceiveURL))
endpointResolver.register(Role.RECEIVER, new DefaultTestEndpoint(sendReceiveURL))
testProfile.setTestEndpointResolver(endpointResolver)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(MessageSize.fixed(messageSize))
testProfile.setInitialRate(rate.intValue())
testProfile.setCeilingRate(ceilingRate.intValue())
testProfile.setRateIncrement(rateIncrement.intValue())
testProfile.setInitialParallelCount(initialParallelCount)
testProfile.setCeilingParallelCount(ceilingParallelCount)
testProfile.setParallelCountIncrement(parallelCountIncrement)

maxLatencyStr = System.getenv("MAXIMUM_LATENCY")
if (maxLatencyStr != null) {
    int maxLatency = Integer.parseInt(maxLatencyStr)
    testProfile.setMaximumLatency(maxLatency)
}

ManagementInterface.setupInterface(managementInterface, inspectorName, testProfile)

IncrementalTestExecutor testExecutor = new IncrementalTestExecutor(maestro, testProfile, distributionStrategy)

description = System.getenv("TEST_DESCRIPTION")
comments = System.getenv("TEST_COMMENTS")

testName = System.getenv("TEST_NAME")
if (testName == null) {
    testName = "fair-incremental"
}

testTags = System.getenv("TEST_TAGS")
labName = System.getenv("LAB_NAME")
sutId = System.getenv("SUT_ID")
sutName = System.getenv("SUT_NAME")
sutVersion = System.getenv("SUT_VERSION")
sutJvmVersion = System.getenv("SUT_JVM_VERSION")
sutOtherInfo = System.getenv("SUT_OTHER_INFO")
sutTags = System.getenv("SUT_TAGS")

TestExecutionInfo testExecutionInfo = TestExecutionInfoBuilder.newBuilder()
        .withDescription(description)
        .withComment(comments)
        .withSutId(sutId)
        .withSutName(sutName)
        .withSutVersion(sutVersion)
        .withSutJvmVersion(sutJvmVersion)
        .withSutOtherInfo(sutOtherInfo)
        .withSutTags(sutTags)
        .withTestName(testName)
        .withTestTags(testTags)
        .withLabName(labName)
        .withScriptName(this.class.getSimpleName())
        .build()

boolean ret = testExecutor.run(testExecutionInfo)

maestro.stop()

if (!ret) {
    System.exit(1)
}

System.exit(0)


