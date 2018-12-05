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
import org.maestro.client.exchange.MaestroTopics
import org.maestro.common.LogConfigurator
import org.maestro.common.Role
import org.maestro.common.agent.UserCommandData
import org.maestro.common.client.notes.Test
import org.maestro.common.client.notes.TestDetails
import org.maestro.common.client.notes.TestExecutionInfo
import org.maestro.common.client.notes.TestExecutionInfoBuilder
import org.maestro.common.duration.TestDurationBuilder
import org.maestro.tests.AbstractTestProfile

import org.maestro.tests.cluster.NonAssigningStrategy
import org.maestro.tests.flex.FlexibleTestExecutor
import org.maestro.tests.flex.singlepoint.FlexibleTestProfile

/**
 * This test executes tests via Maestro Agent
 */
class Executor extends FlexibleTestExecutor {
    private Maestro maestro

    Executor(Maestro maestro, AbstractTestProfile testProfile) {
        super(maestro, testProfile, new NonAssigningStrategy(maestro))

        this.maestro = maestro
    }

    void startServices() {
        String testParams = System.getenv("TEST_PARAMS")

        UserCommandData userCommandData = new UserCommandData(0, testParams);

        maestro.userCommand(MaestroTopics.peerTopic(Role.AGENT), userCommandData)
    }

    @Override
    boolean run(final String scriptName, final String description, final String comments) {
        final TestDetails testDetails = new TestDetails(description, comments);

        String testName = System.getenv("TEST_NAME")
        if (testName == null) {
            testName = "flexible"
        }

        final Test test = new Test(Test.NEXT, Test.NEXT, testName, scriptName, testDetails);

        return run(test)
    }
}

/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker URL was not given"

    System.exit(1)
}

brokerURL = System.getenv("SEND_RECEIVE_URL")
if (brokerURL == null) {
    println "Error: the send/receive URL was not given"

    System.exit(1)
}

sourceURL = System.getenv("SOURCE_URL")
if (sourceURL == null) {
    println "Error: the source url was not set"
    System.exit(1)
}

duration = System.getenv("TEST_DURATION")
if (duration == null) {
    println "Error: the test duration was not given"

    System.exit(1)
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

println "Creating the profile"
FlexibleTestProfile testProfile = new FlexibleTestProfile()

testProfile.setSendReceiveURL(brokerURL)
testProfile.setSourceURL(sourceURL)

branch = System.getenv("SOURCE_BRANCH")
testProfile.setBranch(branch)

testProfile.setDuration(TestDurationBuilder.build(duration));

println "Creating the executor"
Executor executor = new Executor(maestro, testProfile)

description = System.getenv("TEST_DESCRIPTION")
comments = System.getenv("TEST_COMMENTS")

testName = System.getenv("TEST_NAME")
if (testName == null) {
    testName = "flexible"
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

int ret = 0

try {
    println "Running the test"
    if (!executor.run(testExecutionInfo)) {
        ret = 1
    }
} finally {
    println "Stopping the workers on the cluster if they haven't already done so"
    maestro.stopAll()

    println "Stopping Maestro client"
    maestro.stop()
}

if (ret == 0) {
    println "Test completed successfully"
}
else {
    println "Test completed with errors"
}
System.exit(ret)
