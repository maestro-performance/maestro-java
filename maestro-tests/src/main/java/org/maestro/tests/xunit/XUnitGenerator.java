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
 *
 */

package org.maestro.tests.xunit;



import org.apache.commons.io.FileUtils;
import org.maestro.client.notes.InternalError;
import org.maestro.client.notes.TestFailedNotification;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.Test;
import org.maestro.common.xunit.Error;
import org.maestro.common.xunit.Failure;
import org.maestro.common.xunit.TestCase;
import org.maestro.common.xunit.TestSuite;
import org.maestro.common.xunit.TestSuites;
import org.maestro.common.xunit.writer.XunitWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.maestro.tests.utils.IgnoredErrorUtils.isIgnored;

public class XUnitGenerator {
    private static final Logger logger = LoggerFactory.getLogger(XUnitGenerator.class);

    private static TestSuites convertToTestSuites(final Test test, List<? extends MaestroNote> results, long duration) {
        TestSuite testSuite = new TestSuite();
        testSuite.setId("1");
        testSuite.setTests(1);


        for (MaestroNote maestroNote : results) {
            TestCase testCase = new TestCase();

            testCase.setAssertions(1);
            testCase.setClassName(test.getScriptName());
            testCase.setName(generateName(test));

            testCase.setTime(Duration.ofSeconds(duration));

            if (maestroNote instanceof TestFailedNotification) {
                TestFailedNotification testFailedNotification = (TestFailedNotification) maestroNote;

                if (!isIgnored(testFailedNotification)) {
                    Failure failure = new Failure();

                    failure.setMessage(testFailedNotification.getMessage());
                    testCase.setFailure(failure);
                }
            }
            if (maestroNote instanceof InternalError) {
                InternalError internalError = (InternalError) maestroNote;

                Error error = new Error();

                error.setMessage(internalError.getMessage());
                testCase.setError(error);
            }

            testSuite.getTestCaseList().add(testCase);
        }

        TestSuites testSuites = new TestSuites();
        testSuites.getTestSuiteList().add(testSuite);

        return testSuites;
    }


    /**
     * Generate the xUnit file
     * @param test the test case
     * @param results the results as returned by the peers
     * @param start the test start time so that the duration can be calculated
     */
    public static void generate(final Test test, List<? extends MaestroNote> results, Instant start) {
        Instant end = Instant.now();

        Duration elapsed = Duration.between(start, end);
        logger.info("Test run in {} seconds", elapsed.getSeconds());

        generate(test, results, elapsed.getSeconds());
    }


    /**
     * Generate the xUnit file
     * @param test the test case
     * @param results the results as returned by the peers
     * @param duration the test duration
     */
    public static void generate(final Test test, List<? extends MaestroNote> results, long duration) {
        String xUnitDir = System.getenv("TEST_XUNIT_DIR");
        if (xUnitDir == null) {
            logger.info("Skipping xunit file generation because TEST_XUNIT_DIR environment is not set");

            return;
        }

        File file = new File(xUnitDir, generateName(test) + ".xml");

        try {
            FileUtils.forceMkdirParent(file);

            TestSuites testSuites = convertToTestSuites(test, results, duration);

            XunitWriter xunitWriter = new XunitWriter();

            xunitWriter.saveToXML(file, testSuites);

        } catch (IOException e) {
            logger.error("Failed to generate the xunit file: {}", e.getMessage());
        }
    }

    private static String generateName(final Test test) {
        String testName = System.getenv("TEST_XUNIT_NAME");

        if (testName == null) {
            return test.getTestName() + "-" + test.getTestNumber() + "-" + test.getTestIteration();
        }

        return testName;
    }
}
