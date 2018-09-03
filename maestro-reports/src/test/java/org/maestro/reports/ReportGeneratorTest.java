/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.maestro.reports;

import org.junit.Before;
import org.junit.Test;
import org.maestro.common.LogConfigurator;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.SystemProperties;
import org.maestro.common.test.TestProperties;
import org.maestro.plotter.common.exceptions.EmptyDataSet;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ReportGeneratorTest {
    private static final String HOST_01 = "fake-01.host.com";
    private static final String HOST_02 = "fake-02.host.com";
    private static final String HOST_03 = "fake-03.host.com";


    @Before
    public void setUp() {
        LogConfigurator.silent();
    }

    private void validateReportFile(final File baseDir, final List<String> files, final List<String> ignoreList) {
        for (String reportFileName : files) {
            File reportFile = new File(baseDir, reportFileName);

            if (!ignoreList.contains(reportFileName)) {
                assertTrue("Report file " + reportFile + " does not exist", reportFile.exists());
            }
        }
    }

    private void validateReceiverReport(File baseDir, final List<String> ignoreList) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "rate.properties",
                "rate.png", "receiverd-latency_90.png", "receiverd-latency_99.png",
                "receiverd-latency_all.png", TestProperties.FILENAME, SystemProperties.FILENAME), ignoreList);
    }

    private void validateSenderReport(File baseDir, final List<String> ignoreList) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "rate.properties",
                "rate.png", TestProperties.FILENAME, SystemProperties.FILENAME), ignoreList);
    }

    private void validateInspectorReport(File baseDir, final List<String> ignoreList) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "heap.png", "queues.png"), ignoreList);
    }


    private void validateDirectoryContents(File baseDir, final List<String> ignoreList) {
        if (baseDir.getParentFile().getParentFile().getName().equals("receiver")) {
            validateReceiverReport(new File(baseDir, HOST_01), ignoreList);
            validateReceiverReport(new File(baseDir, HOST_02), ignoreList);
        }
        else {
            if (baseDir.getParentFile().getParentFile().getName().equals("sender")) {
                validateSenderReport(new File(baseDir, HOST_01), ignoreList);
                validateSenderReport(new File(baseDir, HOST_02), ignoreList);
            }
            else {
                if (baseDir.getParentFile().getParentFile().getName().equals("inspector")) {
                    validateInspectorReport(new File(baseDir, HOST_03), ignoreList);
                }
            }
        }

    }

    private void validateResultSubDirectoryStructure(File baseDir, int start, int testCount, final List<String> ignoreList) {
        for (int i = start; i < testCount; i++) {
            File testResultSubDir = new File(baseDir, Integer.toString(i));

            assertTrue("Test result sub-directory is missing: " + testResultSubDir, testResultSubDir.exists());
            validateDirectoryContents(testResultSubDir, ignoreList);
        }
    }

    private void validateResultDirectoryStructure(File baseDir, final List<String> ignoreList) {
        File successDir = new File(baseDir, "success");
        File failedDir = new File(baseDir, "failed");

        assertTrue("Neither the success nor the failed directory exists",
                successDir.exists() || failedDir.exists());

        if (successDir.exists()) {
            validateResultSubDirectoryStructure(successDir, 0, 4, ignoreList);
        }

        if (failedDir.exists()) {
            validateResultSubDirectoryStructure(failedDir, 4, 4, ignoreList);
        }
    }

    private void validateRoleDirectoryStructure(final File baseDir, final List<String> role,
                                                final List<String> ignoreList) {
        for (String roleName : role) {
            File rolePathDir = new File(baseDir, roleName);

            assertTrue("Directory for role " + roleName + " does not exist", rolePathDir.exists());
            validateResultDirectoryStructure(rolePathDir, ignoreList);
        }
    }

    @Test(timeout = 65000)
    public void testGenerate() {
        String path = this.getClass().getResource("/data-ok").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        try {
            reportGenerator.generate();

            File indexFile = new File(path, "index.html");
            assertTrue("Index file does not exist: " + indexFile, indexFile.exists());

            validateRoleDirectoryStructure(new File(path),
                    Arrays.asList("receiver", "sender", "inspector"), Collections.emptyList());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Ensures that the report is generated even if critical information is missing
     */
    @Test(timeout = 65000)
    public void testGenerateMissingLatency() {
        String path = this.getClass().getResource("/data-missing-latency").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        try {
            reportGenerator.generate();

            File indexFile = new File(path, "index.html");
            assertTrue("Index file does not exist: " + indexFile, indexFile.exists());

            validateRoleDirectoryStructure(new File(path),
                    Arrays.asList("receiver", "sender"),
                    Arrays.asList("receiverd-latency_90.png", "receiverd-latency_99.png", "receiverd-latency_all.png"));
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Ensures proper handling of invalid reports (ie.: empty reports marked as successful should be ignored)
     */
    @Test(timeout = 40000)
    public void testInvalidReport() {
        String path = this.getClass().getResource("/data-empty-sender-rate-records").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        try {
            reportGenerator.generate();
        }
        catch (Exception e) {
            if (e instanceof EmptyDataSet) {
                File indexFile = new File(path, "index.html");
                assertFalse("Index file does not exist: " + indexFile, indexFile.exists());
            }

            throw e;
        }
    }


    @Test(timeout = 60000)
    public void testInspectorOnly() {
        String path = this.getClass().getResource("/data-ok").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        reportGenerator.generate();

        File indexFile = new File(path, "index.html");
        assertTrue("Index file does not exist: " + indexFile, indexFile.exists());

        validateRoleDirectoryStructure(new File(path),
                Collections.singletonList("inspector"), Collections.emptyList());
    }

    @Test(timeout = 60000)
    public void testInspectorMissingData() {
        String path = this.getClass().getResource("/data-inspector-missing").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        reportGenerator.generate();

        File indexFile = new File(path, "index.html");
        assertTrue("Index file does not exist: " + indexFile, indexFile.exists());

        validateRoleDirectoryStructure(new File(path),
                Collections.singletonList("inspector"), Collections.emptyList());
    }

    @Test(timeout = 60000, expected = MaestroException.class)
    public void testInvalidStructure() {
        String path = this.getClass().getResource("/data-inv-dir-structure").getPath();

        ReportGenerator reportGenerator = new ReportGenerator(path);

        reportGenerator.generate();
    }
}
