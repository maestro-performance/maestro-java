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

import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.client.Maestro;
import org.maestro.tests.AbstractTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateTestProfile extends AbstractTestProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestProfile.class);

    protected int rate;
    protected int parallelCount;
    private String brokerURL;

    private int maximumLatency = 600;
    private TestDuration duration;
    private String messageSize;

    public FixedRateTestProfile() {

    }

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

    public void setDuration(TestDuration duration) {
        this.duration = duration;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    @Override
    public void apply(Maestro maestro) throws MaestroException {
        logger.info("Setting endpoint URL to {}", getBrokerURL());
        maestro.setBroker(getBrokerURL());

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
