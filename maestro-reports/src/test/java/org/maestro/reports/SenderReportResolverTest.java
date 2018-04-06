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

package org.maestro.reports;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class SenderReportResolverTest {
    private static final String BASE_URL = "http://localhost:5006/";

    @Test
    public void testSuccessFiles() {
        ReportResolver reportResolver = new SenderReportResolver();

        List<String> successFiles = reportResolver.getSuccessFiles(BASE_URL);
        assertEquals("List size does not match the expected size", 2, successFiles.size());

        assertEquals("The sender rate file does not match the expected sender rate file",
                BASE_URL + "/logs/tests/lastSuccessful/senderd-rate.csv.gz", successFiles.get(0));
        assertEquals("The test properties file does not match the expected file",
                BASE_URL + "/logs/tests/lastSuccessful/test.properties", successFiles.get(1));
    }

    @Test
    public void testFailedFiles() {
        ReportResolver reportResolver = new SenderReportResolver();

        List<String> failedFiles = reportResolver.getFailedFiles(BASE_URL);
        assertEquals("List size does not match the expected size", 2, failedFiles.size());

        assertEquals("The sender rate file does not match the expected sender rate file",
                BASE_URL + "/logs/tests/lastFailed/senderd-rate.csv.gz", failedFiles.get(0));
        assertEquals("The test properties file does not match the expected file",
                BASE_URL + "/logs/tests/lastFailed/test.properties", failedFiles.get(1));
    }
}
