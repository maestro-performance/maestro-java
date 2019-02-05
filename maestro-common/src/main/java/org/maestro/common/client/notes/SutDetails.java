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

package org.maestro.common.client.notes;

public class SutDetails {
    public static final int UNSPECIFIED = -1;

    private final int sutId;
    private final String sutName;
    private final String sutVersion;
    private final String sutJvmVersion;
    private final String sutOtherInfo;
    private final String sutTags;
    private final String labName;
    private final String testTags;

    public SutDetails(int sutId, String sutName, String sutVersion, String sutJvmVersion, String sutOtherInfo, String sutTags, String labName, String testTags) {
        this.sutId = sutId;
        this.sutName = sutName;
        this.sutVersion = sutVersion;
        this.sutJvmVersion = sutJvmVersion;
        this.sutOtherInfo = sutOtherInfo;
        this.sutTags = sutTags;
        this.labName = labName;
        this.testTags = testTags;
    }

    public SutDetails(String sutName, String sutVersion, String sutJvmVersion, String sutOtherInfo, String sutTags,
                      String labName, String testTags) {
        this.sutId = UNSPECIFIED;
        this.sutName = sutName;
        this.sutVersion = sutVersion;
        this.sutJvmVersion = sutJvmVersion;
        this.sutOtherInfo = sutOtherInfo;
        this.sutTags = sutTags;
        this.labName = labName;
        this.testTags = testTags;
    }

    public SutDetails(int sutId, String labName, String testTags) {
        this.sutId = sutId;
        this.sutName = "";
        this.sutVersion = "";
        this.sutJvmVersion = "";
        this.sutOtherInfo = "";
        this.sutTags = "";
        this.labName = labName;
        this.testTags = testTags;
    }

    public int getSutId() {
        return sutId;
    }

    public String getSutName() {
        return sutName;
    }

    public String getSutVersion() {
        return sutVersion;
    }

    public String getSutJvmVersion() {
        return sutJvmVersion;
    }

    public String getSutOtherInfo() {
        return sutOtherInfo;
    }

    public String getSutTags() {
        return sutTags;
    }

    public String getLabName() {
        return labName;
    }

    public String getTestTags() {
        return testTags;
    }

    @Override
    public String toString() {
        return "SutDetails{" +
                "sutId=" + sutId +
                ", sutName='" + sutName + '\'' +
                ", sutVersion='" + sutVersion + '\'' +
                ", sutJvmVersion='" + sutJvmVersion + '\'' +
                ", sutOtherInfo='" + sutOtherInfo + '\'' +
                ", sutTags='" + sutTags + '\'' +
                ", labName='" + labName + '\'' +
                ", testTags='" + testTags + '\'' +
                '}';
    }
}
