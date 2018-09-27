package org.maestro.common.test;

import org.maestro.common.test.properties.annotations.PropertyConsumer;
import org.maestro.common.test.properties.annotations.PropertyName;
import org.maestro.common.test.properties.annotations.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Worker's system properties written by every worker.
 */
@PropertyName(name = "worker")
public class SystemProperties extends CommonProperties {
    public static final String FILENAME = "system.properties";
    public static int UNSET_INT = 0;

    private static final Logger logger = LoggerFactory.getLogger(InspectorProperties.class);

    private String jvmName;
    private String jvmVersion;
    private String javaVersion;
    private String javaHome;
    private String operatingSystemName;
    private String operatingSystemArch;
    private String operatingSystemVersion;
    private long systemCpuCount;
    private long jvmMaxMemory;

    /**
     * Method for load system properties saved in the file.
     * @param systemProperties A file object pointing to the file to be loaded
     * @throws IOException Exception when input file does not exists
     */
    public void load(final File systemProperties) throws IOException {
        logger.trace("Reading properties from {}", systemProperties.getPath());

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(systemProperties)) {
            prop.load(in);

            jvmName = prop.getProperty("workerJvmName");
            jvmVersion = prop.getProperty("workerJvmVersion");
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

            String jvmMaxMemoryStr = prop.getProperty("workerJvmMaxMemory");
            if (jvmMaxMemoryStr != null) {
                jvmMaxMemory = Long.parseLong(jvmMaxMemoryStr);
            }
            else {
                jvmMaxMemory = UNSET_INT;
            }
            logger.debug("Read properties: {}", this.toString());
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    @PropertyProvider(name="jvmName")
    public String getWorkerJvmName() {
        return jvmName;
    }


    @PropertyConsumer(name="jvmName")
    public void setWorkerJvmName(final String jvmName) {
        this.jvmName = jvmName;
    }

    @PropertyProvider(name="jvmVersion")
    public String getWorkerJvmVersion() {
        return jvmVersion;
    }


    @PropertyConsumer(name="jvmVersion")
    public void setWorkerJvmVersion(final String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }


    @PropertyProvider(name="jvmJavaVersion")
    public String getWorkerJavaVersion() {
        return javaVersion;
    }


    @PropertyConsumer(name="jvmJavaVersion")
    public void setWorkerJavaVersion(final String javaVersion) {
        this.javaVersion = javaVersion;
    }


    @PropertyProvider(name="jvmJavaHome")
    public String getWorkerJavaHome() {
        return javaHome;
    }

    @PropertyConsumer(name="jvmJavaHome")
    public void setWorkerJavaHome(final String javaHome) {
        this.javaHome = javaHome;
    }

    @PropertyProvider(name="operatingSystemName")
    public String getWorkerOperatingSystemName() {
        return operatingSystemName;
    }


    @PropertyConsumer(name="operatingSystemName")
    public void setWorkerOperatingSystemName(final String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
    }

    @PropertyProvider(name="operatingSystemArch")
    public String getWorkerOperatingSystemArch() {
        return operatingSystemArch;
    }

    @PropertyConsumer(name="operatingSystemArch")
    public void setWorkerOperatingSystemArch(final String operatingSystemArch) {
        this.operatingSystemArch = operatingSystemArch;
    }

    @PropertyProvider(name="operatingSystemVersion")
    public String getWorkerOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    @PropertyConsumer(name="operatingSystemVersion")
    public void setWorkerOperatingSystemVersion(final String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }


    @PropertyProvider(name="operatingSystemCpuCount")
    public long getWorkerSystemCpuCount() {
        return systemCpuCount;
    }

    public void setWorkerSystemCpuCount(long systemCpuCount) {
        this.systemCpuCount = systemCpuCount;
    }

    @PropertyConsumer(name="operatingSystemCpuCount")
    public void setWorkerSystemCpuCount(final String systemCpuCount) {
        setWorkerSystemCpuCount(Long.parseLong(systemCpuCount));
    }

    @PropertyProvider(name="operatingSystemMemory")
    public long getWorkerSystemMemory() {
        return jvmMaxMemory;
    }

    public void setWorkerJVMMaxMemory(long systemMemory) {
        this.jvmMaxMemory = systemMemory;
    }

    @PropertyConsumer(name="operatingSystemMemory")
    public void setWorkerJVMMaxMemory(final String systemMemory) {
        setWorkerJVMMaxMemory(Long.parseLong(systemMemory));
    }

    @Override
    public String toString() {
        return "SystemProperties{" +
                "jvmName='" + jvmName + '\'' +
                ", jvmVersion='" + jvmVersion + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", javaHome='" + javaHome + '\'' +
                ", operatingSystemName='" + operatingSystemName + '\'' +
                ", operatingSystemArch='" + operatingSystemArch + '\'' +
                ", operatingSystemVersion='" + operatingSystemVersion + '\'' +
                ", systemCpuCount=" + systemCpuCount +
                ", jvmMaxMemory=" + jvmMaxMemory +
                "} " + super.toString();
    }
}
