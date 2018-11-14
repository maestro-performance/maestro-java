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

import java.util.Objects;

/**
 * Test details
 */
public class TestDetails {
    private String testDescription = "";
    private String testComments = "";

    public TestDetails() {
    }

    /**
     * Constructor
     * @param testDescription the test description
     * @param testComments the test comments
     */
    public TestDetails(final String testDescription, final String testComments) {

        if (testDescription != null) {
            this.testDescription = testDescription;
        }

        if (testComments != null) {
            this.testComments = testComments;
        }
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(final String testDescription) {
        this.testDescription = testDescription;
    }

    public String getTestComments() {
        return testComments;
    }

    public void setTestComments(final String testComments) {
        this.testComments = testComments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestDetails that = (TestDetails) o;
        return Objects.equals(testDescription, that.testDescription) &&
                Objects.equals(testComments, that.testComments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testDescription, testComments);
    }

    @Override
    public String toString() {
        return "TestDetails{" +
                "testDescription='" + testDescription + '\'' +
                ", testComments='" + testComments + '\'' +
                '}';
    }
}
