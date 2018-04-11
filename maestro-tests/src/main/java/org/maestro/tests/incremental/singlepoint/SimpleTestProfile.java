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

package org.maestro.tests.incremental.singlepoint;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.client.Maestro;
import org.maestro.tests.SinglePointProfile;
import org.maestro.tests.incremental.IncrementalTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTestProfile extends IncrementalTestProfile implements SinglePointProfile {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestProfile.class);

    private String brokerURL;

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public void apply(Maestro maestro) throws MaestroException {
        logger.info("Setting broker to {}", getBrokerURL());
        maestro.setBroker(getBrokerURL());

        logger.info("Setting rate to {}", getRate());
        maestro.setRate(rate);

        logger.info("Rate increment value is {}", getRateIncrement());

        logger.info("Setting parallel count to {}", this.parallelCount);
        maestro.setParallelCount(this.parallelCount);

        logger.info("Parallel count increment value is {}", getParallelCountIncrement());

        logger.info("Setting duration to {}", getDuration());
        maestro.setDuration(this.getDuration().toString());

        logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
        maestro.setFCL(getMaximumLatency());

        // Variable message messageSize
        maestro.setMessageSize(getMessageSize());
    }
}
