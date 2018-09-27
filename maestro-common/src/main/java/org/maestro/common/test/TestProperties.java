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

import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.test.properties.annotations.PropertyConsumer;
import org.maestro.common.test.properties.annotations.PropertyName;
import org.maestro.common.test.properties.annotations.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Test properties used/saved by maestro testing peers
 */
@SuppressWarnings("unused")
@PropertyName(name = "")
public class TestProperties extends CommonProperties {
    public static String FILENAME = "test.properties";
    
    private static final Logger logger = LoggerFactory.getLogger(TestProperties.class);

    private String brokerUri;
    private String durationType;
    private long duration;

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

            super.load(prop);
        } catch (Throwable t) {
            logger.error("Invalid data when processing file {}", testProperties.getPath(), t);
            throw t;
        }

        logger.debug("Read properties: {}", this.toString());
    }

    @PropertyProvider(name="brokerUri", join = false)
    public String getBrokerUri() {
        return brokerUri;
    }

    @PropertyConsumer(name="brokerUri", join = false)
    public void setBrokerUri(final String brokerUri) {
        this.brokerUri = brokerUri;
    }

    @PropertyProvider(name="durationType", join = false)
    public String getDurationType() {
        return durationType;
    }

    @PropertyConsumer(name="durationType", join = false)
    public void setDurationType(final String durationType) {
        this.durationType = durationType;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @PropertyConsumer(name="duration", join = false)
    public void setDuration(final String duration) {
        this.duration = Long.parseLong(duration);
    }

    public void setDurationFromSpec(final String durationSpec) throws DurationParseException {
        TestDuration td = TestDurationBuilder.build(durationSpec);

        this.duration = td.getNumericDuration();
        this.durationType = td.durationTypeName();
    }

    public long getDuration() {
        return duration;
    }

    @PropertyProvider(name="duration", join = false)
    public String getDurationAsString() {
        return Long.toString(getDuration());
    }

    @PropertyProvider(name="apiName", join = false)
    public String getApiName() {
        return apiName;
    }

    @PropertyConsumer(name="apiName", join = false)
    public void setApiName(final String apiName) {
        this.apiName = apiName;
    }

    @PropertyProvider(name="apiVersion", join = false)
    public String getApiVersion() {
        return apiVersion;
    }


    @PropertyConsumer(name="apiVersion", join = false)
    public void setApiVersion(final String apiVersion) {
        this.apiVersion = apiVersion;
    }


    @PropertyProvider(name="fcl", join = false)
    public int getFcl() {
        return fcl;
    }


    public void setFcl(int fcl) {
        this.fcl = fcl;
    }

    @PropertyConsumer(name="fcl", join = false)
    public void setFcl(final String fcl) {
        setFcl(Integer.parseInt(fcl));
    }

    @PropertyProvider(name="protocol", join = false)
    public String getProtocol() {
        return protocol;
    }

    @PropertyConsumer(name="protocol", join = false)
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    @PropertyProvider(name="limitDestinations", join = false)
    public int getLimitDestinations() {
        return limitDestinations;
    }

    public void setLimitDestinations(int limitDestinations) {
        this.limitDestinations = limitDestinations;
    }


    @PropertyConsumer(name="limitDestinations", join = false)
    public void setLimitDestinations(final String limitDestinationsStr) {
        setLimitDestinations(Integer.parseInt(limitDestinationsStr));
    }

    @Override
    public String toString() {
        return "TestProperties{" +
                "brokerUri='" + brokerUri + '\'' +
                ", durationType='" + durationType + '\'' +
                ", duration=" + duration +
                ", fcl=" + fcl +
                ", apiName='" + apiName + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", protocol='" + protocol + '\'' +
                ", limitDestinations=" + limitDestinations +
                "} " + super.toString();
    }
}
