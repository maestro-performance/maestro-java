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

public class SutNodeInfo {
    private int sutNodeId;
    private String sutNodeName;
    private String sutNodeOsName;
    private String sutNodeOsArch;
    private String sutNodeOsVersion;
    private String sutNodeOsOther;
    private String sutNodeHwName;
    private String sutNodeHwModel;
    private String sutNodeHwCpu;
    private int sutNodeHwCpuCount;
    private int sutNodeHwRam;
    private String sutNodeHwDiskType;
    private String sutNodeHwOther;

    public int getSutNodeId() {
        return sutNodeId;
    }

    public void setSutNodeId(int sutNodeId) {
        this.sutNodeId = sutNodeId;
    }

    public String getSutNodeName() {
        return sutNodeName;
    }

    public void setSutNodeName(String sutNodeName) {
        this.sutNodeName = sutNodeName;
    }

    public String getSutNodeOsName() {
        return sutNodeOsName;
    }

    public void setSutNodeOsName(String sutNodeOsName) {
        this.sutNodeOsName = sutNodeOsName;
    }

    public String getSutNodeOsArch() {
        return sutNodeOsArch;
    }

    public void setSutNodeOsArch(String sutNodeOsArch) {
        this.sutNodeOsArch = sutNodeOsArch;
    }

    public String getSutNodeOsVersion() {
        return sutNodeOsVersion;
    }

    public void setSutNodeOsVersion(String sutNodeOsVersion) {
        this.sutNodeOsVersion = sutNodeOsVersion;
    }

    public String getSutNodeOsOther() {
        return sutNodeOsOther;
    }

    public void setSutNodeOsOther(String sutNodeOsOther) {
        this.sutNodeOsOther = sutNodeOsOther;
    }

    public String getSutNodeHwName() {
        return sutNodeHwName;
    }

    public void setSutNodeHwName(String sutNodeHwName) {
        this.sutNodeHwName = sutNodeHwName;
    }

    public String getSutNodeHwModel() {
        return sutNodeHwModel;
    }

    public void setSutNodeHwModel(String sutNodeHwModel) {
        this.sutNodeHwModel = sutNodeHwModel;
    }

    public String getSutNodeHwCpu() {
        return sutNodeHwCpu;
    }

    public void setSutNodeHwCpu(String sutNodeHwCpu) {
        this.sutNodeHwCpu = sutNodeHwCpu;
    }

    public int getSutNodeHwCpuCount() {
        return sutNodeHwCpuCount;
    }

    public void setSutNodeHwCpuCount(int sutNodeHwCpuCount) {
        this.sutNodeHwCpuCount = sutNodeHwCpuCount;
    }

    public int getSutNodeHwRam() {
        return sutNodeHwRam;
    }

    public void setSutNodeHwRam(int sutNodeHwRam) {
        this.sutNodeHwRam = sutNodeHwRam;
    }

    public String getSutNodeHwDiskType() {
        return sutNodeHwDiskType;
    }

    public void setSutNodeHwDiskType(String sutNodeHwDiskType) {
        this.sutNodeHwDiskType = sutNodeHwDiskType;
    }

    public String getSutNodeHwOther() {
        return sutNodeHwOther;
    }

    public void setSutNodeHwOther(String sutNodeHwOther) {
        this.sutNodeHwOther = sutNodeHwOther;
    }
}
