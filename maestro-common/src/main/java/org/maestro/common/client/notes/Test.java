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
 *
 */

package org.maestro.common.client.notes;

public class Test {
    public static final int NEXT = -1;
    public static final int LAST = -2;

    private final int testNumber;
    private final int testIteration;
    private final String testName;
    private final String scriptName;

    private final TestDetails testDetails;

    public Test(int testNumber, int testIteration, final String testName, final String scriptName,
                final TestDetails testDetails)
    {
        this.testNumber = testNumber;
        this.testIteration = testIteration;
        this.testName = testName;
        this.scriptName = scriptName;
        this.testDetails = testDetails;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public int getTestIteration() {
        return testIteration;
    }

    public String getTestName() {
        return testName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public TestDetails getTestDetails() {
        return testDetails;
    }

    @Override
    public String toString() {
        return "Test{" +
                "testNumber=" + testNumber +
                ", testIteration=" + testIteration +
                ", testName='" + testName + '\'' +
                ", scriptName='" + scriptName + '\'' +
                ", testDetails=" + testDetails +
                '}';
    }
}
