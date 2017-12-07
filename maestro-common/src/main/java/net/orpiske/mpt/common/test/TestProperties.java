package net.orpiske.mpt.common.test;

import net.orpiske.mpt.common.content.ContentStrategy;
import net.orpiske.mpt.common.content.ContentStrategyFactory;
import net.orpiske.mpt.common.content.MessageSize;
import net.orpiske.mpt.common.duration.DurationTime;
import net.orpiske.mpt.common.duration.TestDuration;
import net.orpiske.mpt.common.duration.TestDurationBuilder;
import net.orpiske.mpt.common.exceptions.DurationParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;


/**
 * Test properties used/saved by maestro testing peers
 */
public class TestProperties implements MaestroTestProperties {
    private static final Logger logger = LoggerFactory.getLogger(TestProperties.class);

    private String brokerUri;
    private String durationType;
    private long duration;
    private int parallelCount;
    private long messageSize;
    private boolean variableSize;
    private int rate;
    private int fcl;
    private String apiName;
    private String apiVersion;
    private String protocol;

    public void load(File testProperties) throws IOException {
        logger.trace("Reading properties from {}", testProperties.getPath());

        Properties prop = new Properties();

        try (FileInputStream in = new FileInputStream(testProperties)) {
            prop.load(in);

            brokerUri = prop.getProperty("brokerUri");
            durationType = prop.getProperty("durationType");
            duration = Long.parseLong(prop.getProperty("duration"));
            parallelCount = Integer.parseInt(prop.getProperty("parallelCount"));
            messageSize = Long.parseLong(prop.getProperty("messageSize"));

            // Optional
            String varSizeStr = prop.getProperty("variableSize");

            if (varSizeStr != null && varSizeStr.equals("1")) {
                variableSize = true;
            }

            rate = Integer.parseInt(prop.getProperty("rate"));

            // Optional stuff
            String fclStr = prop.getProperty("fcl");

            if (fclStr != null) {
                fcl = Integer.parseInt(fclStr);
            }

            apiName = prop.getProperty("apiName");
            apiVersion = prop.getProperty("apiVersion");
            protocol = prop.getProperty("protocol");
        }

    }

    public void write(File testProperties) throws IOException {
        logger.trace("Writing properties to {}", testProperties.getPath());

        Properties prop = new Properties();

        prop.setProperty("brokerUri", brokerUri);
        prop.setProperty("durationType", durationType);
        prop.setProperty("duration", Long.toString(duration));
        prop.setProperty("parallelCount", Integer.toString(parallelCount));
        prop.setProperty("messageSize", Long.toString(messageSize));
        prop.setProperty("variableSize", variableSize ? "1" : "0");
        prop.setProperty("rate", Integer.toString(rate));
        prop.setProperty("fcl", Integer.toString(fcl));
        prop.setProperty("apiName", apiName);
        prop.setProperty("apiVersion", apiVersion);
        prop.setProperty("protocol", protocol);

        try (FileOutputStream fos = new FileOutputStream(testProperties)) {
            prop.store(fos, "mpt");
        }
    }

    public String getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerUri(String brokerUri) {
        this.brokerUri = brokerUri;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDuration(String durationSpec) throws DurationParseException {
        TestDuration td = TestDurationBuilder.build(durationSpec);

        this.duration = td.getNumericDuration();
        this.durationType = td.durationTypeName();
    }

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }

    public void setParallelCount(String parallelCount) {
        this.parallelCount = Integer.parseInt(parallelCount);
    }

    public void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }

    public void setMessageSize(String messageSize) {
        if (MessageSize.isVariable(messageSize)) {
            setVariableSize(true);
        }

        this.messageSize = MessageSize.toSizeFromSpec(messageSize);
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setRate(String rate) {
        this.rate = Integer.parseInt(rate);
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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

    public boolean isVariableSize() {
        return variableSize;
    }

    public void setVariableSize(boolean variableSize) {
        this.variableSize = variableSize;
    }

    public int getFcl() {
        return fcl;
    }

    public void setFcl(int fcl) {
        this.fcl = fcl;
    }

    public void setFcl(String fcl) {
        this.fcl = Integer.parseInt(fcl);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
