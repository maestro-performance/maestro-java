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

import org.apache.commons.io.FilenameUtils;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.node.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ReportFile {
    private static final Logger logger = LoggerFactory.getLogger(ReportFile.class);

    private File sourceFile;
    private final File normalizedFile;

    private boolean reportSuccessful = true;
    private Throwable reportFailure;
    private NodeType nodeType;
    private final String nodeHost;
    private int testNum;
    private boolean testSuccessful = false;

    public ReportFile(File sourceFile, File normalizedFile) {
        this.sourceFile = sourceFile;
        this.normalizedFile = normalizedFile;

        nodeType = NodeType.parse(normalizedFile.getName());

        File hostDir = normalizedFile.getParentFile();
        if (hostDir == null) {
            logger.warn("The host directory is not present for file {}", normalizedFile);
            throw new MaestroException("Invalid directory structure for file " + sourceFile
                    + ": host directory not present on the file structure");
        }

        nodeHost = FilenameUtils.getBaseName(hostDir.getName());

        File testNumDir = hostDir.getParentFile();
        if (testNumDir == null) {
            logger.warn("The test number directory is not present for file {}", normalizedFile);
            throw new MaestroException("Invalid directory structure for file " + sourceFile
                    + ": test number directory not present on the file structure");
        }

        try {
            testNum = Integer.parseInt(FilenameUtils.getBaseName(testNumDir.getName()));
        }
        catch (RuntimeException e) {
            logger.error("Incorrect report directory layout for: {}", normalizedFile.getPath(), e);
            throw e;
        }

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

    public File getReportDir() {
        return sourceFile.getParentFile();
    }

    public File getNormalizedFile() {
        return this.normalizedFile;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportFile that = (ReportFile) o;

        if (reportSuccessful != that.reportSuccessful) return false;
        if (testNum != that.testNum) return false;
        if (testSuccessful != that.testSuccessful) return false;
        if (normalizedFile != null ? !normalizedFile.equals(that.normalizedFile) : that.normalizedFile != null) return false;
        if (reportFailure != null ? !reportFailure.equals(that.reportFailure) : that.reportFailure != null)
            return false;
        if (nodeType != that.nodeType) return false;
        return nodeHost != null ? nodeHost.equals(that.nodeHost) : that.nodeHost == null;
    }

    @Override
    public int hashCode() {
        int result = normalizedFile != null ? normalizedFile.hashCode() : 0;
        result = 31 * result + (reportSuccessful ? 1 : 0);
        result = 31 * result + (reportFailure != null ? reportFailure.hashCode() : 0);
        result = 31 * result + (nodeType != null ? nodeType.hashCode() : 0);
        result = 31 * result + (nodeHost != null ? nodeHost.hashCode() : 0);
        result = 31 * result + testNum;
        result = 31 * result + (testSuccessful ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReportFile{" +
                "sourceFile=" + sourceFile +
                ", normalizedFile=" + normalizedFile +
                ", reportSuccessful=" + reportSuccessful +
                ", reportFailure=" + reportFailure +
                ", nodeType=" + nodeType +
                ", nodeHost='" + nodeHost + '\'' +
                ", testNum=" + testNum +
                ", testSuccessful=" + testSuccessful +
                '}';
    }

    public ReportDirInfo getReportDirInfo() throws IOException {
        return new ReportDirInfo(sourceFile.getParentFile());
    }
}
