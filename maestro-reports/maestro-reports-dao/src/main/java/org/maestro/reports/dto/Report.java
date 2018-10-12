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

package org.maestro.reports.dto;

import org.maestro.common.ResultStrings;
import org.maestro.common.exceptions.MaestroException;

import java.util.List;

public class Report {
    private int reportId;
    private int testId;
    private int testNumber;
    private String testName;
    private String testScript;
    private String testHost;
    private String testHostRole;
    private String testResult;
    private String location;
    private boolean aggregated;

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestScript() {
        return testScript;
    }

    public void setTestScript(String testScript) {
        this.testScript = testScript;
    }

    public String getTestHost() {
        return testHost;
    }

    public void setTestHost(String testHost) {
        this.testHost = testHost;
    }

    public String getTestHostRole() {
        return testHostRole;
    }

    public void setTestHostRole(String testHostRole) {
        this.testHostRole = testHostRole;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAggregated() {
        return aggregated;
    }

    public void setAggregated(boolean aggregated) {
        this.aggregated = aggregated;
    }

    /**
     * Aggregates a set of reports with the given location
     * @param reports the list of reports to aggregate
     * @param location the location of the aggregated reports
     * @return A new Report object instance with the aggregated details
     */
    public static Report aggregate(final List<Report> reports, final String location) {
        Report report = new Report();

        if (reports == null || reports.isEmpty()) {
            throw new MaestroException("Cannot aggregate an empty list of reports");
        }

        Report firstReport = reports.get(0);
        report.setTestId(firstReport.testId);
        report.setTestNumber(firstReport.testNumber);
        report.setTestName(firstReport.testName);
        report.setTestScript(firstReport.testScript);
        report.setTestHost(null);
        report.setAggregated(true);

        long failures = reports.stream().filter(Report::isAggregated).count();
        if (failures > 0) {
            report.setTestResult(ResultStrings.FAILED);
        }
        else {
            report.setTestResult(ResultStrings.SUCCESS);
        }

        report.setLocation(location);
        return report;
    }
}
