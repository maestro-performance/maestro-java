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

import org.maestro.client.Maestro
import org.maestro.common.Role
import org.maestro.common.client.notes.TestExecutionInfo
import org.maestro.common.client.notes.TestExecutionInfoBuilder
import org.maestro.tests.cluster.DistributionStrategyFactory
import org.maestro.tests.rate.AbstractFixedRateExecutor
import org.maestro.tests.rate.FixedRateTestExecutorFactory
import org.maestro.tests.rate.FixedRateTestProfile
import org.maestro.common.LogConfigurator
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

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

maxLatency = System.getenv("MAXIMUM_LATENCY")

managementInterface = System.getenv("MANAGEMENT_INTERFACE")
inspectorName = System.getenv("INSPECTOR_NAME")
warmUp = System.getenv("WARM_UP")

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

distributionStrategy = DistributionStrategyFactory.createStrategy(System.getenv("DISTRIBUTION_STRATEGY"), maestro)

FixedRateTestProfile testProfile = new FixedRateTestProfile()
TestEndpointResolver endpointResolver = TestEndpointResolverFactory.createTestEndpointResolver(System.getenv("ENDPOINT_RESOLVER_NAME"))

endpointResolver.register(Role.SENDER, new DefaultTestEndpoint(sendReceiveURL))
endpointResolver.register(Role.RECEIVER, new DefaultTestEndpoint(sendReceiveURL))
testProfile.setTestEndpointResolver(endpointResolver)

testProfile.setDuration(TestDurationBuilder.build(duration))
testProfile.setMessageSize(messageSize)

if (maxLatency != null) {
    testProfile.setMaximumLatency(Integer.parseInt(maxLatency))
}

testProfile.setRate(Integer.parseInt(rate))
testProfile.setParallelCount(Integer.parseInt(parallelCount))

ManagementInterface.setupInterface(managementInterface, inspectorName, testProfile)

AbstractFixedRateExecutor testExecutor = FixedRateTestExecutorFactory.newTestExecutor(maestro, testProfile, warmUp,
        distributionStrategy)

description = System.getenv("TEST_DESCRIPTION")
comments = System.getenv("TEST_COMMENTS")

testName = System.getenv("TEST_NAME")
if (testName == null) {
    testName = "fixed-rate"
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


