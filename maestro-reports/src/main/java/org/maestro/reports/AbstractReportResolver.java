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

import java.util.LinkedList;
import java.util.List;

public class AbstractReportResolver implements ReportResolver {
    private static final String LAST_SUCCESSFUL_DIR = "lastSuccessful";
    private static final String LAST_FAILED_DIR = "lastFailed";

    private final List<String> failedFiles = new LinkedList<>();
    private final List<String> successFiles = new LinkedList<>();
    private final String[] fileArray;
    private final String baseURL;

    public AbstractReportResolver(final String baseURL, final String[] fileArray) {
        this.baseURL = baseURL;
        this.fileArray = fileArray;

        for (String file : fileArray) {
            failedFiles.add(baseURL + "/logs/tests/" + LAST_FAILED_DIR + "/" + file);
            successFiles.add(baseURL + "/logs/tests/" + LAST_SUCCESSFUL_DIR + "/" + file);
        }
    }

    @Override
    public List<String> getFailedFiles() {
        return failedFiles;
    }

    @Override
    public List<String> getSuccessFiles() {
        return successFiles;
    }

    @Override
    public List<String> getTestFiles(final String testNum) {
        List<String> files = new LinkedList<>();

        for (String file : fileArray) {
            files.add(baseURL + "/logs/tests/" + testNum + "/" + file);
        }

        return files;
    }
}
