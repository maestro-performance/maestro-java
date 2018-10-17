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

public class TestDetails {
    private String testDescription = "";
    private String testComments = "";

    public TestDetails() {
    }

    public TestDetails(String testDescription, String testComments) {

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
}
