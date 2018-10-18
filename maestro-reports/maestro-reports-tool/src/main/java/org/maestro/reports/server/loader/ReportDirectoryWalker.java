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

package org.maestro.reports.server.loader;

import org.apache.commons.io.DirectoryWalker;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ReportDirectoryWalker extends DirectoryWalker<Report> {
    private static final Logger logger = LoggerFactory.getLogger(ReportDirectoryWalker.class);


    /**
     * Loads report data in a directory with format like this: maestro/baseline/id/$id/number/$number/$role/$result/$number/$host
     * @param file
     * @param depth
     * @param results
     * @throws IOException
     */

    @Override
    protected void handleFile(File file, int depth, Collection<Report> results) throws IOException {
        if (file.getName().equals("test.properties") || file.getName().equals("inspector.properties")) {
            File hostDir = file.getParentFile();
            String host = hostDir.getName();

            File legacyTestNumberDir = hostDir.getParentFile();
            int legacyTestNumber = Integer.parseInt(legacyTestNumberDir.getName());

            File testResultStringDir = legacyTestNumberDir.getParentFile();
            String testResultString = testResultStringDir.getName();

            File resourceRoleDir = testResultStringDir.getParentFile();
            String resourceRole = resourceRoleDir.getName();

            File testNumberDir = resourceRoleDir.getParentFile();
            int testNumber = Integer.parseInt(testNumberDir.getName());

            if (testNumber != legacyTestNumber) {
                logger.trace("Ignoring duplicated/orphaned test dir {} != {}", legacyTestNumber, testNumber);
            }
            else {
                logger.trace("Test properties found: {}", file);

                File testIdDir = testNumberDir.getParentFile().getParentFile();

                int testId = 0;

                try {
                    testId = Integer.parseInt(testIdDir.getName());
                }
                catch (Throwable t) {
                    logger.error("Unable to convert directory {}", file);
                    return;
                }

                File testNameDir = testIdDir.getParentFile().getParentFile();
                String testName = testNameDir.getName();

                Report report = new Report();

                report.setTestHost(host);
                report.setTestHostRole(resourceRole);
                report.setTestName(testName);
                report.setTestNumber(testNumber);
                report.setTestId(testId);
                report.setLocation(hostDir.getPath());
                report.setTestResult(testResultString);
                report.setTestScript("undefined");
                report.setTestDescription("Auto-loaded");
                report.setTestComments("Legacy test");
                report.setValid(true);
                report.setRetired(false);

                results.add(report);
            }
        }
    }


    public Set<Report> load(final File reportsDir) {
        Set<Report> reportSet = new TreeSet<>();

        try {
            if (reportsDir.exists()) {
                walk(reportsDir, reportSet);
            }
            else {
                logger.error("The reports directory does not exist: {}", reportsDir.getPath());
            }
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: {}", e.getMessage(), e);
            logger.error("Returning a partial list of all the reports due to errors");
        }

        return reportSet;

    }
}
