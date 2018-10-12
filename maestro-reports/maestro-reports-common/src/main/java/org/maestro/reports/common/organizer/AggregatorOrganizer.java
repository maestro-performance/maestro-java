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

package org.maestro.reports.common.organizer;


import java.io.File;

public class AggregatorOrganizer implements Organizer<String> {
    protected final String baseDir;
    protected int testId;
    protected int testNumber;

    public AggregatorOrganizer(final String baseDir) {
        this.baseDir = baseDir;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }

    protected String combine() {
        return baseDir + File.separator + "id" + File.separator + testId + File.separator + "number" + File.separator +
                testNumber + File.separator + "aggregated";
    }

    @Override
    public String organize(final String meta) {
        return combine();
    }
}
