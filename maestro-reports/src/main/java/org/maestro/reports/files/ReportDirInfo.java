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

package org.maestro.reports.files;

import org.maestro.common.test.TestProperties;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class ReportDirInfo {
    private final File reportDir;
    private final String nodeType;

    private final String nodeHost;
    private final int testNum;
    private boolean testSuccessful = false;

    private final String resultTypeString;

    private final TestProperties testProperties;

    /**
     * Holds information about the report directory and its structure. It is used
     * to generate the index pages.
     *
     * @param reportDir The report directory (ie.: the parent directory for a ReportFile)
     * @throws IOException
     */
    ReportDirInfo(final File reportDir) throws IOException {
        this.reportDir = reportDir;

        nodeHost = reportDir.getName();

        File testNumDir = reportDir.getParentFile();
        testNum = Integer.parseInt(FilenameUtils.getBaseName(testNumDir.getName()));

        File resultType = testNumDir.getParentFile();
        if (resultType.getName().contains("success")) {
            testSuccessful = true;
        }
        resultTypeString = FilenameUtils.getBaseName(resultType.getName());

        this.nodeType = FilenameUtils.getBaseName(resultType.getParentFile().getName());

        testProperties = new TestProperties();

        testProperties.load(new File(reportDir, "test.properties"));
    }

    public String getBrokerUri() {
        return testProperties.getBrokerUri();
    }

    public String getDurationType() {
        return testProperties.getDurationType();
    }

    public String getApiName() {
        return testProperties.getApiName();
    }

    public String getApiVersion() {
        return testProperties.getApiVersion();
    }

    public long getDuration() {
        return testProperties.getDuration();
    }

    public long getMessageSize() {
        return testProperties.getMessageSize();
    }

    public int getRate() {
        return testProperties.getRate();
    }

    public int getParallelCount() {
        return testProperties.getParallelCount();
    }

    public boolean isVariableSize() {
        return testProperties.isVariableSize();
    }

    public int getFcl() {
        return testProperties.getFcl();
    }

    public String getReportDir() {
        return reportDir.getPath();
    }

    public String getReportDirRelative() {
        return getNodeType() + File.separator + resultTypeString + File.separator + testNum + File.separator + nodeHost;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public int getTestNum() {
        return testNum;
    }

    public String getResultTypeString() {
        return resultTypeString;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }

    public void setTestSuccessful(boolean testSuccessful) {
        this.testSuccessful = testSuccessful;
    }

    @Override
    public int hashCode() {
        return reportDir != null ? reportDir.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportDirInfo that = (ReportDirInfo) o;

        return reportDir != null ? reportDir.equals(that.reportDir) : that.reportDir == null;
    }
}
