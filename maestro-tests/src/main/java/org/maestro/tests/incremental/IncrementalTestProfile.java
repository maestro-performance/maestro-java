/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.tests.incremental;

import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.duration.TestDuration;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.maestro.client.Maestro.set;

@SuppressWarnings("unused")
public class IncrementalTestProfile extends AbstractTestProfile {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestProfile.class);

    private int initialRate = 100;
    private int ceilingRate = 500;
    private int rateIncrement = 50;

    private int initialParallelCount = 1;
    private int ceilingParallelCount = 16;
    private int parallelCountIncrement = 1;

    protected int rate = initialRate;
    protected int parallelCount = initialParallelCount;

    private int maximumLatency = 600;
    private TestDuration duration;
    private String messageSize;

    public IncrementalTestProfile() {

    }

    public int getInitialRate() {
        return initialRate;
    }

    public void setInitialRate(int initialRate) {
        this.initialRate = initialRate;
        this.rate = initialRate;
    }

    public int getCeilingRate() {
        return ceilingRate;
    }

    public void setCeilingRate(int ceilingRate) {
        this.ceilingRate = ceilingRate;
    }

    public int getInitialParallelCount() {
        return initialParallelCount;
    }

    public void setInitialParallelCount(int initialParallelCount) {
        this.initialParallelCount = initialParallelCount;
        this.parallelCount = initialParallelCount;
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public int getCeilingParallelCount() {
        return ceilingParallelCount;
    }

    public void setCeilingParallelCount(int ceilingParallelCount) {
        this.ceilingParallelCount = ceilingParallelCount;
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


    public int getRateIncrement() {
        return rateIncrement;
    }

    public void setRateIncrement(int rateIncrement) {
        this.rateIncrement = rateIncrement;
    }

    public int getParallelCountIncrement() {
        return parallelCountIncrement;
    }

    public void setParallelCountIncrement(int parallelCountIncrement) {
        this.parallelCountIncrement = parallelCountIncrement;
    }

    public boolean isOverCeiling() {
        return (rate > ceilingRate && parallelCount >= ceilingParallelCount);
    }

    public void increment() {
        rate += rateIncrement;
        incrementTestExecutionNumber();

        if (rate > ceilingRate) {
            logger.info("Reached the virtual ceiling");
            parallelCount += parallelCountIncrement;

            if (parallelCount >= ceilingParallelCount) {
                logger.info("Reached the virtual ceiling for the parallel count and the test is complete");
            }
            else {
                 logger.info("Increased number of parallel connections to: {}", parallelCount);
                rate = initialRate;
            }
        }

        logger.info("Set target rate to {}", rate);
    }

    @Override
    public long getEstimatedCompletionTime() {
        return getEstimatedCompletionTime(duration, getRate());
    }

    public void apply(final Maestro maestro, final DistributionStrategy distributionStrategy) {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();

        for (PeerEndpoint endpoint : endpoints) {
            final String destination = endpoint.getDestination();
            setSendReceiveURL(maestro, endpoint);

            logger.info("Setting rate to {}", getRate());
            set(maestro::setRate, destination, rate);

            logger.info("Rate increment value is {}", getRateIncrement());

            logger.info("Setting parallel count to {}", this.parallelCount);
            set(maestro::setParallelCount, destination, this.parallelCount);

            logger.info("Parallel count increment value is {}", getParallelCountIncrement());

            logger.info("Setting duration to {}", getDuration());
            set(maestro::setDuration, destination, this.getDuration().toString());

            logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
            set(maestro::setFCL, destination, getMaximumLatency());

            // Variable message messageSize
            logger.info("Setting message size to: {}", getMessageSize());
            set(maestro::setMessageSize, destination, getMessageSize());

            applyInspector(maestro, endpoint, destination);

            applyAgent(maestro, endpoint, destination);

            logger.info("Estimated time for test completion: {} seconds", getEstimatedCompletionTime());
        }
    }


    @Override
    public String toString() {
        return "IncrementalTestProfile{" +
                "initialRate=" + initialRate +
                ", ceilingRate=" + ceilingRate +
                ", rateIncrement=" + rateIncrement +
                ", initialParallelCount=" + initialParallelCount +
                ", ceilingParallelCount=" + ceilingParallelCount +
                ", parallelCountIncrement=" + parallelCountIncrement +
                ", rate=" + rate +
                ", parallelCount=" + parallelCount +
                ", maximumLatency=" + maximumLatency +
                ", duration=" + duration +
                ", messageSize='" + messageSize + '\'' +
                "} " + super.toString();
    }
}
