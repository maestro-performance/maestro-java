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

package org.maestro.tests.incremental.multipoint;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.client.Maestro;
import org.maestro.tests.MultiPointProfile;
import org.maestro.tests.incremental.IncrementalTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class SimpleTestProfile extends IncrementalTestProfile implements MultiPointProfile {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestProfile.class);
    private final List<EndPoint> endPoints = new LinkedList<>();

    @Override
    public void addEndPoint(EndPoint endPoint) {
        endPoints.add(endPoint);
    }

    @Override
    public List<EndPoint> getEndPoints() {
        return endPoints;
    }

    public void apply(Maestro maestro) throws MaestroException {
        for (EndPoint endPoint : endPoints) {
            logger.info("Setting {} end point to {}", endPoint.getName(), endPoint.getBrokerURL());
            logger.debug(" {} end point located at {}", endPoint.getName(), endPoint.getTopic());

            maestro.setBroker(endPoint.getTopic(), endPoint.getBrokerURL());
        }

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
