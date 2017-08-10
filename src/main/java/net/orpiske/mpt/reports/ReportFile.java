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

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class ReportFile {
    private File file;
    private boolean reportSuccessful = true;
    private Throwable reportFailure;
    private NodeType nodeType;
    private String nodeHost;
    private int testNum;
    private boolean testSuccessful = false;

    public ReportFile(File file) {
        this.file = file;

        nodeType = NodeType.parse(file.getName());

        File hostDir = file.getParentFile();
        nodeHost = FilenameUtils.getBaseName(hostDir.getName());

        File testNumDir = hostDir.getParentFile();
        testNum = Integer.parseInt(FilenameUtils.getBaseName(testNumDir.getName()));

        File resultType = testNumDir.getParentFile();
        if (resultType.getName().contains("success")) {
            testSuccessful = true;
        }
    }

    public boolean isReportSuccessful() {
        return reportSuccessful;
    }

    public void setReportSuccessful(boolean reportSuccessful) {
        this.reportSuccessful = reportSuccessful;
    }

    public Throwable getReportFailure() {
        return reportFailure;
    }

    public void setReportFailure(Throwable reportFailure) {
        this.reportFailure = reportFailure;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    protected void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public int getTestNum() {
        return testNum;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }

    public String getReportDir() {
        return file.getParent();
    }
}
