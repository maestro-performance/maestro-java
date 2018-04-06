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

import org.maestro.common.content.MessageSize;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Test properties used/saved by maestro testing peers
 */
@SuppressWarnings("unused")
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

    // 1 = legacy behavior
    private int limitDestinations = 1;

    public void load(final File testProperties) throws IOException {
        logger.debug("Reading properties from {}", testProperties.getPath());

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

            String limitDestinationsStr = prop.getProperty("limitDestinations");
            if (limitDestinationsStr != null) {
                limitDestinations = Integer.parseInt(limitDestinationsStr);
            }
        }

        logger.debug("Read properties: {}", this.toString());
    }

    public void write(final File testProperties) throws IOException {
        logger.debug("Writing properties to {}", testProperties.getPath());
        logger.debug("Wrote properties: {}", this.toString());

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
        prop.setProperty("limitDestinations", Integer.toString(limitDestinations));

        try (FileOutputStream fos = new FileOutputStream(testProperties)) {
            prop.store(fos, "mpt");
        }
    }

    public String getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerUri(final String brokerUri) {
        this.brokerUri = brokerUri;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(final String durationType) {
        this.durationType = durationType;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDuration(final String durationSpec) throws DurationParseException {
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

    public void setMessageSize(final String messageSize) {
        if (MessageSize.isVariable(messageSize)) {
            setVariableSize(true);
        }

        this.messageSize = MessageSize.toSizeFromSpec(messageSize);
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setRate(final String rate) {
        this.rate = Integer.parseInt(rate);
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(final String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(final String apiVersion) {
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

    public void setFcl(final String fcl) {
        this.fcl = Integer.parseInt(fcl);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public int getLimitDestinations() {
        return limitDestinations;
    }

    public void setLimitDestinations(int limitDestinations) {
        this.limitDestinations = limitDestinations;
    }

    @Override
    public String toString() {
        return "TestProperties{" +
                "brokerUri='" + brokerUri + '\'' +
                ", durationType='" + durationType + '\'' +
                ", duration=" + duration +
                ", parallelCount=" + parallelCount +
                ", messageSize=" + messageSize +
                ", variableSize=" + variableSize +
                ", rate=" + rate +
                ", fcl=" + fcl +
                ", apiName='" + apiName + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", protocol='" + protocol + '\'' +
                ", limitDestinations=" + limitDestinations +
                '}';
    }
}
