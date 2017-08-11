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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ReportDirInfo {
    private String reportDir;
    private String nodeType;

    private String nodeHost;
    private int testNum;
    private boolean testSuccessful = false;

    private long duration;
    private long messageSize;
    private int rate;
    private int parallelCount;
    private boolean variableSize;
    private int fcl;

    public ReportDirInfo(String reportDir, String nodeType) {
        this.reportDir = reportDir;
        this.nodeType = nodeType;

        File file = new File(reportDir);

        nodeHost = FilenameUtils.getBaseName(file.getName());

        File testNumDir = file.getParentFile();
        testNum = Integer.parseInt(FilenameUtils.getBaseName(testNumDir.getName()));

        File resultType = testNumDir.getParentFile();
        if (resultType.getName().contains("success")) {
            testSuccessful = true;
        }

        loadProperties(new File(reportDir, "test.properties"));
    }

    private void loadProperties(File testProperties) {
        if (testProperties.exists()) {
            Properties prop = new Properties();

            try (FileInputStream in = new FileInputStream(testProperties)) {
                prop.load(in);

                duration = Long.parseLong(prop.getProperty("duration"));
                messageSize = Long.parseLong(prop.getProperty("messageSize"));
                rate = Integer.parseInt(prop.getProperty("rate"));
                parallelCount = Integer.parseInt(prop.getProperty("parallelCount"));

                // Optional stuff
                String varSizeStr = prop.getProperty("variableSize");

                if (varSizeStr != null && varSizeStr.equals("1")) {
                    variableSize = true;
                }

                // Optional stuff
                String fclStr = prop.getProperty("fcl");

                if (fclStr != null) {
                    fcl = Integer.parseInt(fclStr);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(String reportDir) {
        this.reportDir = reportDir;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public int getTestNum() {
        return testNum;
    }

    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }

    public void setTestSuccessful(boolean testSuccessful) {
        this.testSuccessful = testSuccessful;
    }

    public long getDuration() {
        return duration;
    }

    public long getMessageSize() {
        return messageSize;
    }

    public int getRate() {
        return rate;
    }

    public int getParallelCount() {
        return parallelCount;
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
