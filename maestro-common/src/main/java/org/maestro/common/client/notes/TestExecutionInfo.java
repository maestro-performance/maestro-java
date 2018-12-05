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

package org.maestro.common.client.notes;

/**
 * Container for the test execution information (ie.: test information, SUT information, etc)
 */
public class TestExecutionInfo {
    private Test test;
    private final SutDetails sutDetails;

    /**
     * Constructor
     * @param test test information
     * @param sutDetails optional SUT details
     */
    public TestExecutionInfo(final Test test, final SutDetails sutDetails) {
        this.test = test;

        this.sutDetails = sutDetails;
    }

    public Test getTest() {
        return test;
    }

    public SutDetails getSutDetails() {
        return sutDetails;
    }

    public boolean hasSutDetails() {
        if (sutDetails != null) {
            return true;
        }

        return false;
    }

    public void iterate() {
        test = test.iterate();
    }
}
