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

package org.maestro.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Test properties used/saved by inspectors
 */
@SuppressWarnings("ALL")
public class InspectorProperties extends CommonProperties {
    public static String FILENAME = "inspector.properties";
    public static int UNSET_INT = 0;

    private static final Logger logger = LoggerFactory.getLogger(InspectorProperties.class);

    private String jvmName;
    private String jvmVersion;
    private String jvmPackageVersion;
    private String operatingSystemName;
    private String operatingSystemArch;
    private String operatingSystemVersion;
    private long systemCpuCount;
    private long systemMemory;
    private long systemSwap;
    private String productName;
    private String productVersion;

    public void load(final File testProperties) throws IOException {
        logger.trace("Reading properties from {}", testProperties.getPath());

        String loadedSwap;

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(testProperties)) {
            prop.load(in);

            jvmName = prop.getProperty("jvmName");
            jvmVersion = prop.getProperty("jvmVersion");
            jvmPackageVersion = prop.getProperty("jvmPackageVersion");
            operatingSystemName = prop.getProperty("operatingSystemName");
            operatingSystemArch = prop.getProperty("operatingSystemArch");
            operatingSystemVersion = prop.getProperty("operatingSystemVersion");

            String systemCpuCountStr = prop.getProperty("systemCpuCount");
            if (systemCpuCountStr != null) {
                systemCpuCount = Integer.parseInt(systemCpuCountStr);
            }
            else {
                systemCpuCount = UNSET_INT;
            }

            String systemMemoryStr = prop.getProperty("systemMemory");
            if (systemMemoryStr != null) {
                systemMemory = Long.parseLong(systemMemoryStr);
            }
            else {
                systemMemory = UNSET_INT;
            }

            loadedSwap = prop.getProperty("systemSwap");
            systemSwap = (loadedSwap != null) ? Long.parseLong(loadedSwap) : UNSET_INT;

            productName = prop.getProperty("productName");
            productVersion = prop.getProperty("productVersion");

            super.load(prop);
        }

    }

    public void write(final File testProperties) throws IOException {
        logger.trace("Writing properties to {}", testProperties.getPath());

        Properties prop = new Properties();

        prop.setProperty("jvmName", jvmName);
        prop.setProperty("jvmVersion", jvmVersion);

        if (jvmPackageVersion != null) {
            prop.setProperty("jvmPackageVersion", jvmPackageVersion);
        }

        prop.setProperty("operatingSystemName", operatingSystemName);
        prop.setProperty("operatingSystemArch", operatingSystemArch);
        prop.setProperty("operatingSystemVersion", operatingSystemVersion);
        prop.setProperty("systemCpuCount", Long.toString(systemCpuCount));
        prop.setProperty("systemMemory", Long.toString(systemMemory));
        if (systemSwap != UNSET_INT) {
            prop.setProperty("systemSwap", Long.toString(systemSwap));
        }

        if (productName != null) {
            prop.setProperty("productName", productName);
        }

        if (productVersion != null) {
            prop.setProperty("productVersion", productVersion);
        }

        super.write(prop);

        try (FileOutputStream fos = new FileOutputStream(testProperties)) {
            prop.store(fos, "maestro");
        }
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getJvmPackageVersion() {
        return jvmPackageVersion;
    }

    public void setJvmPackageVersion(String jvmPackageVersion) {
        this.jvmPackageVersion = jvmPackageVersion;
    }

    public String getOperatingSystemName() {
        return operatingSystemName;
    }

    public void setOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    public String getOperatingSystemArch() {
        return operatingSystemArch;
    }

    public void setOperatingSystemArch(String operatingSystemArch) {
        this.operatingSystemArch = operatingSystemArch;
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setOperatingSystemVersion(String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }

    public long getSystemCpuCount() {
        return systemCpuCount;
    }

    public void setSystemCpuCount(long systemCpuCount) {
        this.systemCpuCount = systemCpuCount;
    }

    public long getSystemMemory() {
        return systemMemory;
    }

    public void setSystemMemory(long systemMemory) {
        this.systemMemory = systemMemory;
    }

    public long getSystemSwap() {
        return systemSwap;
    }

    public void setSystemSwap(long systemSwap) {
        this.systemSwap = systemSwap;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }
}
