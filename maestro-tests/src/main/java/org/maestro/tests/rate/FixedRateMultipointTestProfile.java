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

package org.maestro.tests.rate;

import org.maestro.client.Maestro;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.AbstractTestProfile;
import org.maestro.tests.MultiPointProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateMultipointTestProfile extends FixedRateTestProfile implements MultiPointProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateMultipointTestProfile.class);

    protected int rate;
    protected int parallelCount;

    private int maximumLatency = 600;
    private TestDuration duration;
    private String messageSize;

    private final List<EndPoint> endPoints = new LinkedList<>();

    public FixedRateMultipointTestProfile() {

    }

    @Override
    public void addEndPoint(EndPoint endPoint) {
        endPoints.add(endPoint);
    }

    @Override
    public List<EndPoint> getEndPoints() {
        return endPoints;
    }

    @Override
    public void apply(Maestro maestro) throws MaestroException {
        for (EndPoint endPoint : endPoints) {
            logger.info("Setting {} end point to {}", endPoint.getName(), endPoint.getBrokerURL());
            logger.debug(" {} end point located at {}", endPoint.getName(), endPoint.getTopic());

            maestro.setBroker(endPoint.getTopic(), endPoint.getBrokerURL());
        }

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
