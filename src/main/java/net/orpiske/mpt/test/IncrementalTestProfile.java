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
import net.orpiske.mpt.maestro.exceptions.MaestroException;
import net.orpiske.mpt.utils.DurationUtils;
import net.orpiske.mpt.utils.TestDuration;
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
    private TestDuration duration;
    private String size;

    private int testExecutionNumber;

    public IncrementalTestProfile(String brokerURL, int rate, int parallelCount, TestDuration duration, String size) {
        this.brokerURL = brokerURL;
        this.rate = rate;
        this.parallelCount = parallelCount;
        this.duration = duration;
        this.size = size;
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

    public int getTestExecutionNumber() {
        return testExecutionNumber;
    }

    public TestDuration getDuration() {
        return duration;
    }

        public void apply(Maestro maestro) throws MqttException, IOException, MaestroException {
        logger.info("Setting broker to {}", brokerURL);
        maestro.setBroker(brokerURL);

        logger.info("Setting rate to {}", rate);
        maestro.setRate(rate);

        logger.info("Setting parallel count to {}", this.parallelCount);
        maestro.setParallelCount(this.parallelCount);

        logger.info("Setting duration to {}", this.duration);
        maestro.setDuration(this.duration.getDuration());

        logger.info("Setting fail-condition-latency to {}", this.maximumLatency);
        maestro.setFCL(this.maximumLatency);

        // Variable message size
        maestro.setMessageSize(size);
    }



    public void increment() {
        rate += 50;
        testExecutionNumber++;

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
                "brokerURL='" + brokerURL + '\'' +
                ", rate=" + rate +
                ", parallelCount=" + parallelCount +
                ", maximumLatency=" + maximumLatency +
                ", duration=" + duration +
                ", size='" + size + '\'' +
                ", testExecutionNumber=" + testExecutionNumber +
                '}';
    }
}
