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

import org.maestro.client.Maestro;
import org.maestro.client.exchange.support.PeerEndpoint;
import org.maestro.common.Role;
import org.maestro.tests.MultiPointProfile;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.incremental.IncrementalTestProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.maestro.client.Maestro.set;

@SuppressWarnings("unused")
public class SimpleTestProfile extends IncrementalTestProfile implements MultiPointProfile {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTestProfile.class);
    private final Map<Role, TestEndpoint> testEndpoints = new HashMap<>();

    @Override
    public void addEndPoint(final Role role, final TestEndpoint testEndpoint) {
        testEndpoints.put(role, testEndpoint);
    }

    @Override
    public Map<Role, TestEndpoint> getTestEndpoints() {
        return Collections.unmodifiableMap(testEndpoints);
    }


    public void apply(final Maestro maestro, final DistributionStrategy distributionStrategy) {
        Set<PeerEndpoint> endpoints = distributionStrategy.endpoints();

        for (PeerEndpoint endpoint : endpoints) {
            String destination = endpoint.getDestination();
            TestEndpoint testEndpoint = testEndpoints.get(endpoint.getRole());

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
}
