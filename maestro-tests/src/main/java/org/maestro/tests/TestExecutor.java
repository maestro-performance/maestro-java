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

/**
 * A base interface for executing tests
 */
public interface TestExecutor {

    /**
     * Runs the test
     * @param scriptName the test script being used
     * @param description the test description
     * @param comments the test comments
     * @return true if successful or false otherwise
     */
    boolean run(final String scriptName, final String description, final String comments);

    /**
     * Gets the amount of time (in milliseconds) to wait for the SUT to cool down
     * @return the cool down period in milliseconds
     */
    long getCoolDownPeriod();
}
