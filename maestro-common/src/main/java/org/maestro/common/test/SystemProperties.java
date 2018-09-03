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
    public static String FILENAME = "system-properties.ini";
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

        prop.setProperty("javaVersion", javaVersion);
        prop.setProperty("javaHome", javaHome);
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

        try (FileOutputStream fos = new FileOutputStream(systemProperties)) {
            prop.store(fos, "system-info");
        }
    }


    public static String getFILENAME() {
        return FILENAME;
    }

    public static void setFILENAME(String FILENAME) {
        SystemProperties.FILENAME = FILENAME;
    }

    public static int getUnsetInt() {
        return UNSET_INT;
    }

    public static void setUnsetInt(int unsetInt) {
        UNSET_INT = unsetInt;
    }

    public static Logger getLogger() {
        return logger;
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

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
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
