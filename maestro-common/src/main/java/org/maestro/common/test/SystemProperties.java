package org.maestro.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Worker's system properties written by every worker.
 */
public class SystemProperties extends CommonProperties {
    public static final String FILENAME = "system.properties";
    public static int UNSET_INT = 0;

    private static final Logger logger = LoggerFactory.getLogger(InspectorProperties.class);

    private String jvmName;
    private String jvmVersion;
    private String jvmPackageVersion;
    private String javaVersion;
    private String javaHome;
    private String operatingSystemName;
    private String operatingSystemArch;
    private String operatingSystemVersion;
    private long systemCpuCount;
    private long systemMemory;
    private long systemSwap;

    /**
     * Method for load system properties saved in the file.
     * @param systemProperties A file object pointing to the file to be loaded
     * @throws IOException Exception when input file does not exists
     */
    public void load(final File systemProperties) throws IOException {
        logger.trace("Reading properties from {}", systemProperties.getPath());

        String loadedSwap;

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(systemProperties)) {
            prop.load(in);

            jvmName = prop.getProperty("workerJvmName");
            jvmVersion = prop.getProperty("workerJvmVersion");
            jvmPackageVersion = prop.getProperty("workerJvmPackageVersion");
            operatingSystemName = prop.getProperty("workerOperatingSystemName");
            operatingSystemArch = prop.getProperty("workerOperatingSystemArch");
            operatingSystemVersion = prop.getProperty("workerOperatingSystemVersion");

            String systemCpuCountStr = prop.getProperty("workerSystemCpuCount");
            if (systemCpuCountStr != null) {
                systemCpuCount = Integer.parseInt(systemCpuCountStr);
            }
            else {
                systemCpuCount = UNSET_INT;
            }

            String systemMemoryStr = prop.getProperty("workerSystemMemory");
            if (systemMemoryStr != null) {
                systemMemory = Long.parseLong(systemMemoryStr);
            }
            else {
                systemMemory = UNSET_INT;
            }

            loadedSwap = prop.getProperty("workerSystemSwap");
            systemSwap = (loadedSwap != null) ? Long.parseLong(loadedSwap) : UNSET_INT;

            logger.debug("Read properties: {}", this.toString());
        }
    }

    /**
     * Method for write system properties into the file.
     * @param systemProperties A file object pointing to the file to be loaded
     * @throws IOException Exception when input file does not exists
     */
    public void write(final File systemProperties) throws IOException {
        logger.trace("Writing properties to {}", systemProperties.getPath());

        Properties prop = new Properties();

        prop.setProperty("workerJavaVersion", javaVersion);
        prop.setProperty("workerJavaHome", javaHome);
        prop.setProperty("workerJvmName", jvmName);
        prop.setProperty("workerJvmVersion", jvmVersion);

        if (jvmPackageVersion != null) {
            prop.setProperty("workerJvmPackageVersion", jvmPackageVersion);
        }

        prop.setProperty("workerOperatingSystemName", operatingSystemName);
        prop.setProperty("workerOperatingSystemArch", operatingSystemArch);
        prop.setProperty("workerOperatingSystemVersion", operatingSystemVersion);
        prop.setProperty("workerSystemCpuCount", Long.toString(systemCpuCount));
        prop.setProperty("workerSystemMemory", Long.toString(systemMemory));
        if (systemSwap != UNSET_INT) {
            prop.setProperty("workerSystemSwap", Long.toString(systemSwap));
        }

        try (FileOutputStream fos = new FileOutputStream(systemProperties)) {
            prop.store(fos, "worker-system-info");
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getWorkerJvmName() {
        return jvmName;
    }

    public void setWorkerJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getWorkerJvmVersion() {
        return jvmVersion;
    }

    public void setWorkerJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getWorkerJvmPackageVersion() {
        return jvmPackageVersion;
    }

    public void setWorkerJvmPackageVersion(String jvmPackageVersion) {
        this.jvmPackageVersion = jvmPackageVersion;
    }

    public String getWorkerJavaVersion() {
        return javaVersion;
    }

    public void setWorkerJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getWorkerJavaHome() {
        return javaHome;
    }

    public void setWorkerJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getWorkerOperatingSystemName() {
        return operatingSystemName;
    }

    public void setWorkerOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    public String getWorkerOperatingSystemArch() {
        return operatingSystemArch;
    }

    public void setWorkerOperatingSystemArch(String operatingSystemArch) {
        this.operatingSystemArch = operatingSystemArch;
    }

    public String getWorkerOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setWorkerOperatingSystemVersion(String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }

    public long getWorkerSystemCpuCount() {
        return systemCpuCount;
    }

    public void setWorkerSystemCpuCount(long systemCpuCount) {
        this.systemCpuCount = systemCpuCount;
    }

    public long getWorkerSystemMemory() {
        return systemMemory;
    }

    public void setWorkerSystemMemory(long systemMemory) {
        this.systemMemory = systemMemory;
    }

    public long getWorkerSystemSwap() {
        return systemSwap;
    }

    public void setWorkerSystemSwap(long systemSwap) {
        this.systemSwap = systemSwap;
    }

    @Override
    public String toString() {
        return "SystemProperties{" +
                "operatingSystemName='" + operatingSystemName + '\'' +
                ", operatingSystemVersion='" + operatingSystemVersion + '\'' +
                ", operatingSystemArch=" + operatingSystemArch +
                ", systemCpuCount=" + systemCpuCount +
                ", systemMemory=" + systemMemory +
                ", javaVersion=" + javaVersion +
                ", javaHome='" + javaHome + '\'' +
                ", jvmName='" + jvmName + '\'' +
                ", jvmVersion='" + jvmVersion + '\'' +
                "} " + super.toString();
    }
}
