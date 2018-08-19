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
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.Role;
import org.maestro.common.duration.TestDuration;
import org.maestro.tests.MultiPointProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.rate.singlepoint.FixedRateTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.maestro.client.Maestro.set;

/**
 * A test profile for fixed rate tests
 */
public class FixedRateMultipointTestProfile extends FixedRateTestProfile implements MultiPointProfile {
    private static final Logger logger = LoggerFactory.getLogger(FixedRateMultipointTestProfile.class);
    private final Map<Role, TestEndpoint> endPoints = new HashMap<>();

    @Override
    public void addEndPoint(Role role, TestEndpoint testEndpoint) {
        endPoints.put(role, testEndpoint);
    }

    @Override
    public Map<Role, TestEndpoint> getTestEndpoints() {
        return Collections.unmodifiableMap(endPoints);
    }

    @Override
    protected void apply(final Maestro maestro, boolean warmUp, final DistributionStrategy distributionStrategy) {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();

        for (PeerEndpoint endpoint : endpoints) {
            String destination = endpoint.getDestination();
            TestEndpoint testEndpoint = endPoints.get(endpoint.getRole());

            if (testEndpoint == null) {
                if (endpoint.getRole().isWorker()) {
                    logger.info("There is not test end point set for peers w/ role {}", endpoint.getRole());
                }
                else {
                    logger.info("There is no test end point to {}", endpoint.getRole());
                }
            }
            else {
                logger.info("Setting {} end point to {}", endpoint.getRole(), testEndpoint.getSendReceiveURL());

                set(maestro::setBroker, endpoint.getDestination(), testEndpoint.getSendReceiveURL());
            }

            if (warmUp) {
                logger.info("Setting warm up rate to {}", getRate());
                set(maestro::setRate, destination, warmUpRate);

                TestDuration warmUpDuration = getDuration().getWarmUpDuration();
                long balancedDuration = Math.round((double) warmUpDuration.getNumericDuration() / (double) getParallelCount());

                logger.info("Setting warm up duration to {}", balancedDuration);
                set(maestro::setDuration, destination, balancedDuration);
            } else {
                logger.info("Setting test rate to {}", getRate());
                set(maestro::setRate, destination, getRate());

                logger.info("Setting test duration to {}", getDuration());
                set(maestro::setDuration, destination, getDuration().toString());
            }

            logger.info("Setting parallel count to {}", getParallelCount());
            set(maestro::setParallelCount, destination, getParallelCount());

            logger.info("Setting fail-condition-latency to {}", getMaximumLatency());
            set(maestro::setFCL, destination, getMaximumLatency());

            logger.info("Setting message size to {}", getMessageSize());
            set(maestro::setMessageSize, destination, getMessageSize());

            applyInspector(maestro, endpoint, destination);

            applyAgent(maestro, endpoint, destination);
        }
    }


    @Override
    public String toString() {
        return "FixedRateMultipointTestProfile{" +
                "endPoints=" + endPoints +
                "} " + super.toString();
    }
}
