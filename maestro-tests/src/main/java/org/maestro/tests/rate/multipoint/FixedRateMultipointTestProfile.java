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

package org.maestro.tests.rate.multipoint;

import org.maestro.client.Maestro;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.tests.MultiPointProfile;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateMultipointTestProfile extends FixedRateTestProfile implements MultiPointProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateMultipointTestProfile.class);
    private final List<EndPoint> endPoints = new LinkedList<>();

    @Override
    public void addEndPoint(EndPoint endPoint) {
        endPoints.add(endPoint);
    }

    @Override
    public List<EndPoint> getEndPoints() {
        return endPoints;
    }

    @Override
    protected void apply(final Maestro maestro, boolean warmUp) throws MaestroException {
        for (EndPoint endPoint : endPoints) {
            logger.info("Setting {} end point to {}", endPoint.getName(), endPoint.getSendReceiveURL());
            logger.debug(" {} end point located at {}", endPoint.getName(), endPoint.getTopic());

            maestro.setBroker(endPoint.getTopic(), endPoint.getSendReceiveURL());
        }

        if (warmUp) {
            logger.info("Setting warm up rate to {}", getRate());
            maestro.setRate(warmUpRate);

            TestDuration warmUpDuration = getDuration().getWarmUpDuration();
            long balancedDuration = Math.round(warmUpDuration.getNumericDuration() / getParallelCount());

            logger.info("Setting warm up duration to {}", balancedDuration);
            maestro.setDuration(balancedDuration);
        }
        else {
            logger.info("Setting test rate to {}", getRate());
            maestro.setRate(getRate());

            logger.info("Setting test duration to {}", getDuration());
            maestro.setDuration(getDuration().toString());
        }

        logger.info("Setting parallel count to {}", getParallelCount());
        maestro.setParallelCount(getParallelCount());

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
        return "FixedRateMultipointTestProfile{" +
                "endPoints=" + endPoints +
                "} " + super.toString();
    }
}
