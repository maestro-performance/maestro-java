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

package org.maestro.common.xunit.writer;

import org.junit.Test;
import org.maestro.common.xunit.*;
import org.maestro.common.xunit.Error;

import java.io.File;
import java.time.Duration;

import static org.junit.Assert.assertTrue;


public class XunitWriterTest {

    @Test
    public void testSaveXmlSuccess() {
        String path = this.getClass().getResource("/").getPath() + "xunit.success.xml";

        TestCase warmUpTc = new TestCase();
        warmUpTc.setAssertions(1);
        warmUpTc.setClassName("singlepoint.FixedRate");
        warmUpTc.setName("AMQP-s-1024-c-30-ld-10-durable-false");
        warmUpTc.setTime(Duration.ofSeconds(5));

        TestCase testRun = new TestCase();
        testRun.setAssertions(1);
        testRun.setClassName("singlepoint.FixedRate");
        testRun.setName("AMQP-s-1024-c-100-ld-10-durable-false");
        testRun.setTime(Duration.ofSeconds(300));

        TestSuite testSuite = new TestSuite();
        testSuite.setId("0");
        testSuite.setTests(1);

        testSuite.getTestCaseList().add(warmUpTc);
        testSuite.getTestCaseList().add(testRun);

        TestSuites testSuites = new TestSuites();
        testSuites.getTestSuiteList().add(testSuite);

        XunitWriter xunitWriter = new XunitWriter();
        File outFile = new File(path);

        xunitWriter.saveToXML(outFile, testSuites);

        assertTrue(outFile.exists());
    }

    @Test
    public void testSaveXmlWithFailure() {
        String path = this.getClass().getResource("/").getPath() + "xunit.failure.xml";

        TestCase warmUpTc = new TestCase();
        warmUpTc.setAssertions(1);
        warmUpTc.setClassName("singlepoint.FixedRate");
        warmUpTc.setName("AMQP-s-1024-c-30-ld-10-durable-false");
        warmUpTc.setTime(Duration.ofSeconds(5));

        TestCase testRun = new TestCase();
        testRun.setAssertions(1);
        testRun.setClassName("singlepoint.FixedRate");
        testRun.setName("AMQP-s-1024-c-100-ld-10-durable-false");
        testRun.setTime(Duration.ofSeconds(300));

        Failure failure = new Failure();
        failure.setMessage("AMQP Framing Error");
        failure.setContent("SUT disconnected");

        testRun.setFailure(failure);

        TestSuite testSuite = new TestSuite();
        testSuite.setId("0");
        testSuite.setTests(1);

        testSuite.getTestCaseList().add(warmUpTc);
        testSuite.getTestCaseList().add(testRun);

        TestSuites testSuites = new TestSuites();
        testSuites.getTestSuiteList().add(testSuite);

        XunitWriter xunitWriter = new XunitWriter();
        File outFile = new File(path);

        xunitWriter.saveToXML(outFile, testSuites);

        assertTrue(outFile.exists());
    }

    @Test
    public void testSaveXmlWithError() {
        String path = this.getClass().getResource("/").getPath() + "xunit.error.xml";

        TestCase warmUpTc = new TestCase();
        warmUpTc.setAssertions(1);
        warmUpTc.setClassName("singlepoint.FixedRate");
        warmUpTc.setName("AMQP-s-1024-c-30-ld-10-durable-false");
        warmUpTc.setTime(Duration.ofSeconds(5));

        TestCase testRun = new TestCase();
        testRun.setAssertions(1);
        testRun.setClassName("singlepoint.FixedRate");
        testRun.setName("AMQP-s-1024-c-100-ld-10-durable-false");
        testRun.setTime(Duration.ofSeconds(300));

        Error error = new Error();
        error.setMessage("java.lang.NullPointerException");
        error.setContent("Maestro worker failed: java.lang.NullPointerException");

        testRun.setError(error);

        TestSuite testSuite = new TestSuite();
        testSuite.setId("0");
        testSuite.setTests(1);

        testSuite.getTestCaseList().add(warmUpTc);
        testSuite.getTestCaseList().add(testRun);

        TestSuites testSuites = new TestSuites();
        testSuites.getTestSuiteList().add(testSuite);

        XunitWriter xunitWriter = new XunitWriter();
        File outFile = new File(path);

        xunitWriter.saveToXML(outFile, testSuites);

        assertTrue(outFile.exists());
    }


    @Test
    public void testSaveXmlSuccessAndProperties() {
        String path = this.getClass().getResource("/").getPath() + "xunit.success.with.properties.xml";

        TestCase warmUpTc = new TestCase();
        warmUpTc.setAssertions(1);
        warmUpTc.setClassName("singlepoint.FixedRate");
        warmUpTc.setName("AMQP-s-1024-c-30-ld-10-durable-false");
        warmUpTc.setTime(Duration.ofSeconds(5));

        TestCase testRun = new TestCase();
        testRun.setAssertions(1);
        testRun.setClassName("singlepoint.FixedRate");
        testRun.setName("AMQP-s-1024-c-100-ld-10-durable-false");
        testRun.setTime(Duration.ofSeconds(300));

        TestSuite testSuite = new TestSuite();
        testSuite.setId("0");
        testSuite.setTests(1);

        testSuite.getTestCaseList().add(warmUpTc);
        testSuite.getTestCaseList().add(testRun);

        TestSuites testSuites = new TestSuites();
        testSuites.getTestSuiteList().add(testSuite);

        Property messageSize = new Property();

        messageSize.setValue("1024");
        messageSize.setName("size");

        Property pairs = new Property();

        pairs.setValue("100");
        pairs.setName("pairs");

        testSuites.getProperties().getPropertyList().add(messageSize);
        testSuites.getProperties().getPropertyList().add(pairs);


        XunitWriter xunitWriter = new XunitWriter();
        File outFile = new File(path);

        xunitWriter.saveToXML(outFile, testSuites);

        assertTrue(outFile.exists());
    }

}

