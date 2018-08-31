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

package org.maestro.tests;

import org.maestro.client.Maestro;
import org.maestro.tests.cluster.DistributionStrategy;
import org.maestro.tests.support.TestEndpointResolver;

/**
 * A base interface for implementing test profiles.
 *
 * Test profiles provide a mechanism to tweak test execution parameters. They are used by
 * the test executors. For example, a test executor may use a profile to increase or
 * decrease the rate for a test.
 */
@SuppressWarnings("unused")
public interface TestProfile {

    /**
     * Gets an estimate of how much time it will take to complete the test execution
     * @return the estimated duration in seconds
     */
    long getEstimatedCompletionTime();

    /**
     * Get the test execution number
     * @return the test execution number
     */
    int getTestExecutionNumber();

    /**
     * Increment the test execution number
     */
    void incrementTestExecutionNumber();


    /**
     * Sets the test endpoint resolver
     * @param endPointResolver the test endpoint resolver
     */
    void setTestEndpointResolver(TestEndpointResolver endPointResolver);


    /**
     * Apply a test profile using a maestro instance
     * @param maestro the maestro instance to apply the profile to
     * @param distributionStrategy the peer distribution strategy
     */
    void apply(Maestro maestro, DistributionStrategy distributionStrategy);
}
