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

package net.orpiske.mpt.test;

import net.orpiske.mpt.maestro.Maestro;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IncrementalTestProfile {
    private static final Logger logger = LoggerFactory.getLogger(IncrementalTestProfile.class);

    private int INITIAL_RATE = 100;
    private int CEILING_RATE = 500;

    private String brokerURL;
    private int rate = INITIAL_RATE;
    private int parallelCount = 1;
    private final int maximumLatency = 600;
    private long duration;

    public IncrementalTestProfile(String brokerURL, int rate, int parallelCount, long duration) {
        this.brokerURL = brokerURL;
        this.rate = rate;
        this.parallelCount = parallelCount;
        this.duration = duration;
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public int getRate() {
        return rate;
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public int getMaximumLatency() {
        return maximumLatency;
    }

    public long getDuration() {
        return duration;
    }

    public void apply(Maestro maestro) throws MqttException, IOException {
        logger.info("Setting broker to {}", brokerURL);
        maestro.setBroker(brokerURL);

        logger.info("Setting rate to {}", rate);
        maestro.setRate(rate);

        logger.info("Setting parallel count to {}", this.parallelCount);
        maestro.setParallelCount(this.parallelCount);

        logger.info("Setting duration to {}", this.duration);
        maestro.setDuration(this.duration);

        logger.info("Setting fail-condition-latency to {}", this.maximumLatency);
        maestro.setFCL(this.maximumLatency);

        // Variable message size
        maestro.setMessageSize("~256");
    }



    public void increment() {
        rate += 50;

        if (rate > CEILING_RATE) {
            parallelCount++;
            rate = INITIAL_RATE;

            logger.info("Reached the virtual ceiling. Increasing number of parallel connections to: {}",
                    parallelCount);
            logger.info("Setting target rate to {}", rate);
        }
    }

    @Override
    public String toString() {
        return "IncrementalTestProfile{" +
                "rate=" + rate +
                ", parallelCount=" + parallelCount +
                ", maximumLatency=" + maximumLatency +
                ", duration=" + duration +
                '}';
    }
}
