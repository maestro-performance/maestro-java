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

package org.maestro.tests.rate.singlepoint;

import org.maestro.client.Maestro;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.SinglePointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateTestProfile extends AbstractTestProfile implements SinglePointProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestProfile.class);

    protected int rate;
    protected int parallelCount;
    private String brokerURL;

    private int maximumLatency = 600;
    private TestDuration duration;
    private String messageSize;

    private String extPointSource;
    private String extPointBranch;
    private String extPointCommand;

    public String getExtPointSource() {
        return extPointSource;
    }

    public void setExtPointSource(String extPointSource) {
        this.extPointSource = extPointSource;
    }

    public String getExtPointBranch() {
        return extPointBranch;
    }

    public void setExtPointBranch(String extPointBranch) {
        this.extPointBranch = extPointBranch;
    }

    public String getExtPointCommand() {
        return extPointCommand;
    }

    public void setExtPointCommand(String extPointCommand) {
        this.extPointCommand = extPointCommand;
    }

    public FixedRateTestProfile() {}

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }

    public int getParallelCount() {
        return parallelCount;
    }


    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getRate() {
        return rate;
    }


    public int getMaximumLatency() {
        return maximumLatency;
    }

    public void setMaximumLatency(int maximumLatency) {
        this.maximumLatency = maximumLatency;
    }

    public TestDuration getDuration() {
        return duration;
    }

    public void setDuration(final TestDuration duration) {
        this.duration = duration;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getBrokerURL() {
        return getSendReceiveURL();
    }

    public void setBrokerURL(final String brokerURL) {
        setSendReceiveURL(brokerURL);
    }

    @Override
    public void setSendReceiveURL(String url) {
        this.brokerURL = url;
    }

    @Override
    public String getSendReceiveURL() {
        return brokerURL;
    }

    @Override
    public void apply(Maestro maestro) throws MaestroException {
        logger.info("Setting endpoint URL to {}", getSendReceiveURL());
        maestro.setBroker(getSendReceiveURL());

        logger.info("Setting rate to {}", getRate());
        maestro.setRate(rate);

        logger.info("Setting parallel count to {}", this.parallelCount);
        maestro.setParallelCount(this.parallelCount);

        logger.info("Setting duration to {}", getDuration());
        maestro.setDuration(this.getDuration().toString());

        logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
        maestro.setFCL(getMaximumLatency());

        logger.info("Setting message size to {}", getMessageSize());
        maestro.setMessageSize(getMessageSize());

        if (getManagementInterface() != null) {
            if (getInspectorName() != null) {
                logger.info("Setting the management interface to {} using inspector {}", getManagementInterface(),
                        getInspectorName());
                maestro.setManagementInterface(getManagementInterface());
            }
        }

        if (getExtPointSource() != null) {
            if (getExtPointBranch() != null) {
                logger.info("Setting the extension point source to {} using the {} branch", getExtPointSource(),
                        getExtPointBranch());
                maestro.sourceRequest(getExtPointSource(), getExtPointBranch());
            }
        }

        if (getExtPointCommand() != null) {
            logger.info("Setting command to Agent execution to {}", getExtPointCommand());
            maestro.userCommand(0, getExtPointCommand());
        }
    }

    @Override
    public String toString() {
        return "FixedRateTestProfile{" +
                "rate=" + rate +
                ", parallelCount=" + parallelCount +
                ", maximumLatency=" + maximumLatency +
                ", duration=" + duration +
                ", messageSize='" + messageSize + '\'' +
                "} " + super.toString();
    }
}
