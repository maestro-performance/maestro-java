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
public class InspectorProperties implements MaestroTestProperties {
    private static final Logger logger = LoggerFactory.getLogger(InspectorProperties.class);

    private String jvmName;
    private String jvmVersion;
    private String jvmPackageVersion;
    private String operatingSystemName;
    private String operatingSystemArch;
    private String operatingSystemVersion;
    private int systemCpuCount;
    private long systemMemory;
    private long systemSwap;
    private String productName;
    private String productVersion;

    public void load(File testProperties) throws IOException {
        logger.trace("Reading properties from {}", testProperties.getPath());

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(testProperties)) {
            prop.load(in);

            jvmName = prop.getProperty("jvmName");
            jvmVersion = prop.getProperty("jvmVersion");
            jvmPackageVersion = prop.getProperty("jvmPackageVersion");
            operatingSystemName = prop.getProperty("operatingSystemName");
            operatingSystemArch = prop.getProperty("operatingSystemArch");
            operatingSystemVersion = prop.getProperty("operatingSystemVersion");

            systemCpuCount = Integer.parseInt(prop.getProperty("systemCpuCount"));
            systemMemory = Long.parseLong(prop.getProperty("systemMemory"));
            systemSwap = Long.parseLong(prop.getProperty("systemSwap"));

            productName = prop.getProperty("productName");
            productVersion = prop.getProperty("productVersion");
        }

    }

    public void write(File testProperties) throws IOException {
        logger.trace("Writing properties to {}", testProperties.getPath());


        Properties prop = new Properties();

        prop.setProperty("jvmName", jvmName);
        prop.setProperty("jvmVersion", jvmVersion);
        prop.setProperty("jvmPackageVersion", jvmPackageVersion);
        prop.setProperty("operatingSystemName", operatingSystemName);
        prop.setProperty("operatingSystemArch", operatingSystemArch);
        prop.setProperty("operatingSystemVersion", operatingSystemVersion);
        prop.setProperty("systemCpuCount", Integer.toString(systemCpuCount));
        prop.setProperty("systemMemory", Long.toString(systemMemory));
        prop.setProperty("systemSwap", Long.toString(systemSwap));
        prop.setProperty("productName", productName);
        prop.setProperty("productVersion", productVersion);

        try (FileOutputStream fos = new FileOutputStream(testProperties)) {
            prop.store(fos, "mpt");
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

    public int getSystemCpuCount() {
        return systemCpuCount;
    }

    public void setSystemCpuCount(int systemCpuCount) {
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
