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

package org.maestro.tests.rate;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MessageCorrelation;
import org.maestro.common.duration.TestDuration;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.utils.CompletionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.maestro.client.Maestro.set;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateTestProfile extends AbstractTestProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateTestProfile.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private int rate;
    protected int warmUpRate;
    private int parallelCount;
    private int warmUpParallelCount;

    private int maximumLatency = 0;
    private TestDuration duration;
    private String messageSize;

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;

        setWarmUpParallelCount(parallelCount);
    }

    private void setWarmUpParallelCount(int parallelCount) {
        final int ceilingWarmUpPc = config.getInt("warm-up.ceiling.parallel.count", 30);
        if (parallelCount > ceilingWarmUpPc) {
            warmUpParallelCount = ceilingWarmUpPc;
        }
        else {
            warmUpParallelCount = parallelCount;
        }
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public void setRate(int rate) {
        double rateMultiplier = config.getDouble("warm-up.rate.percent", 30);

        this.rate = rate;
        double warmUpTmp = rate * (rateMultiplier / 100);

        if (warmUpTmp > Integer.MAX_VALUE) {
            warmUpRate = Integer.MAX_VALUE;
        }
        else {
            warmUpRate = (int) Math.round(warmUpTmp);
        }
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

    @Override
    public long getEstimatedCompletionTime() {
        return getEstimatedCompletionTime(duration, getRate());
    }

    public long getWarmUpEstimatedCompletionTime() {
        return CompletionTime.estimate(getDuration().getWarmUpDuration(), warmUpRate);
    }

    private void checkReplies(List<? extends MaestroNote> replies) {
        Maestro.checkReplies(replies, "apply");
    }

    protected void apply(final Maestro maestro, boolean warmUp, final DistributionStrategy distributionStrategy) {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();
        List<MessageCorrelation> correlations = new LinkedList<>();

        for (PeerEndpoint endpoint : endpoints) {
            String destination = endpoint.getDestination();
            String roleName = endpoint.getRole().toString();

            setSendReceiveURL(maestro, endpoint, correlations);

            if (warmUp) {
                logger.info("Setting {} warm-up rate to {}", roleName, warmUpRate);
                correlations.add(maestro.setRateAsync(destination, warmUpRate));

                TestDuration warmUpDuration = getDuration().getWarmUpDuration();
                long balancedDuration = Math.round((double) warmUpDuration.getNumericDuration() / (double) getParallelCount());

                logger.info("Setting {} warm-up duration to {}", roleName, balancedDuration);
                correlations.add(maestro.setDurationAsync(destination, balancedDuration));

                logger.info("Setting {} warm-up parallel count to {}", roleName, this.warmUpParallelCount);
                set(maestro::setParallelCount, destination, this.warmUpParallelCount);
                correlations.add(maestro.setParallelCountAsync(destination, warmUpParallelCount));
            }
            else {
                logger.info("Setting {} test rate to {}", roleName, getRate());
                set(maestro::setRate, destination, rate);
                correlations.add(maestro.setRateAsync(destination, rate));

                logger.info("Setting {} test duration to {}", roleName, getDuration());
                correlations.add(maestro.setDurationAsync(destination, this.getDuration().toString()));

                logger.info("Setting {} parallel count to {}", roleName, this.parallelCount);
                correlations.add(maestro.setParallelCountAsync(destination, parallelCount));
            }

            logger.info("Setting {} fail-condition-latency to {}", roleName, getMaximumLatency());
            correlations.add(maestro.setFCLAsync(destination, getMaximumLatency()));

            logger.info("Setting {} message size to {}", roleName, getMessageSize());
            correlations.add(maestro.setMessageSizeAsync(destination, getMessageSize()));

            applyInspector(maestro, endpoint, destination, correlations);

            applyAgent(maestro, endpoint, destination, correlations);
        }

        maestro.waitFor(correlations).thenAccept(this::checkReplies);
    }

    @Override
    public void apply(final Maestro maestro, final DistributionStrategy distributionStrategy) {
        logger.info("Applying test execution profile");
        apply(maestro, false, distributionStrategy);
        logger.info("Estimated time for test completion: {} seconds", getEstimatedCompletionTime());
    }

    public void warmUp(final Maestro maestro, final DistributionStrategy distributionStrategy) {
        logger.info("Applying test warm-up profile");
        apply(maestro, true, distributionStrategy);
        logger.info("Estimated time for warm-up completion: {} seconds", getWarmUpEstimatedCompletionTime());
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
