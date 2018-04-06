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

import java.util.List;

/**
 * A report file resolver gets a list of report files for a peer
 */
public interface ReportResolver {

    /**
     * Gets a list of files to be downloaded when a test is successful
     * @param baseURL the base URL from which the report files need to be resolved
     * @return A list of files (as a URL to the file to be downloaded)
     */
    List<String> getSuccessFiles(final String baseURL);

    /**
     * Gets a list of files to be downloaded when a test is failed
     * @param baseURL the base URL from which the report files need to be resolved
     * @return A list of files (as a URL to the file to be downloaded)
     */
    List<String> getFailedFiles(final String baseURL);

    /**
     * Gets a list of files to be downloaded when a test is complete regardless of the result
     * @param baseURL the base URL from which the report files need to be resolved
     * @param testNum the test number
     * @return A list of files (as a URL to the file to be downloaded)
     */
    List<String> getTestFiles(final String baseURL, final String testNum);
}
