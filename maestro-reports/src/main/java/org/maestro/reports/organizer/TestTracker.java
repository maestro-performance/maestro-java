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

package org.maestro.reports.organizer;

/**
 * Keeps track of the current test execution
 */
public class TestTracker {
    private int currentTest = 0;

    /**
     * The test number as a String
     * @return gets the test number as a String
     */
    public String currentTestString() {
        return java.lang.Integer.toString(currentTest);
    }

    /**
     * Gets the current test number
     * @return the current test number
     */
    public int currentTest() {
        return currentTest;
    }

    /**
     * Sets the current test number
     * @param value the current test number
     */
    public void setCurrentTest(int value) {
        currentTest = value;
    }
}
