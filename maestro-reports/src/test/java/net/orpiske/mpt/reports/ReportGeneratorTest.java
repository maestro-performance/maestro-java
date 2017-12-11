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

package net.orpiske.mpt.reports;

import net.orpiske.mpt.common.Constants;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ReportGeneratorTest {
    private static final String HOST_01 = "fake-01.host.com";
    private static final String HOST_02 = "fake-02.host.com";
    private static final String HOST_03 = "fake-03.host.com";

    private void validateReportFile(File baseDir, List<String> files) {
        for (String reportFileName : files) {
            File reportFile = new File(baseDir, reportFileName);

            assertTrue("Report file " + reportFile + " does not exist", reportFile.exists());
        }
    }

    private void validateReceiverReport(File baseDir) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "rate.properties",
                "receiverd-rate_rate.png", "test.properties"));
    }

    private void validateSenderReport(File baseDir) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "rate.properties",
                "senderd-rate_rate.png", "test.properties"));
    }

    private void validateInspectorReport(File baseDir) {
        validateReportFile(baseDir, Arrays.asList("favicon.png", "index.html", "broker-jvm-inspector_tenured_memory.png",
                "broker-jvm-inspector_memory.png", "broker-jvm-inspector_pm_memory.png",
                "broker-jvm-inspector_survivor_memory.png", "broker-jvm-inspector_queue_data.png",
                "broker-jvm-inspector_eden_memory.png", "broker.properties",
                "test.properties"));
    }

    private void validateDirectoryContents(File baseDir) {
        if (baseDir.getParentFile().getParent().equals("receiver")) {
            validateReceiverReport(new File(baseDir, HOST_01));
            validateReceiverReport(new File(baseDir, HOST_02));
        }
        else {
            if (baseDir.getParentFile().getParent().equals("sender")) {
                validateSenderReport(new File(baseDir, HOST_01));
                validateSenderReport(new File(baseDir, HOST_02));
            }
            else {
                if (baseDir.getParentFile().getParent().equals("inspector")) {
                    validateInspectorReport(new File(baseDir, HOST_03));
                }
            }
        }

    }

    private void validateResultSubDirectoryStructure(File baseDir, int start, int testCount) {
        for (int i = start; i < testCount; i++) {
            File testResultSubDir = new File(baseDir, Integer.toString(i));

            assertTrue("Test result sub-directory is missing: " + testResultSubDir, testResultSubDir.exists());
        }
    }

    private void validateResultDirectoryStructure(File baseDir) {
        File successDir = new File(baseDir, "success");
        File failedDir = new File(baseDir, "failed");

        assertTrue("Neither the success nor the failed directory exists",
                successDir.exists() || failedDir.exists());

        if (successDir.exists()) {
            validateResultSubDirectoryStructure(successDir, 0, 4);
        }

        if (failedDir.exists()) {
            validateResultSubDirectoryStructure(failedDir, 4, 4);
        }
    }

    private void validateRoleDirectoryStructure(File baseDir, final List<String> role) {
        for (String roleName : role) {
            File rolePathDir = new File(baseDir, roleName);

            assertTrue("Directory for role " + roleName + " does not exist", rolePathDir.exists());
            validateResultDirectoryStructure(rolePathDir);
        }
    }

    @Test
    public void testGenerate() {
        String path = this.getClass().getResource("/data-ok").getPath();

        System.out.println(Constants.HOME_DIR);

        ReportGenerator.generate(path);

        File indexFile = new File(path, "index.html");
        assertTrue("Index file does not exist: " + indexFile, indexFile.exists());

        validateRoleDirectoryStructure(new File(path), Arrays.asList("inspector", "receiver", "sender"));
    }
}
