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

import java.util.ArrayList;
import java.util.List;

/**
 * A basic resolver for test logs stored in the /logs/tests/ directory of the data server
 */
public abstract class AbstractReportResolver implements ReportResolver {
    protected static final String LAST_SUCCESSFUL_DIR = "lastSuccessful";
    protected static final String LAST_FAILED_DIR = "lastFailed";
    protected static final String CONTEXT = "/logs/tests/";

    private final String[] fileArray;

    public AbstractReportResolver(final String[] fileArray) {
        this.fileArray = fileArray;
    }

    protected List<String> listBuilder(String baseURL, String lastFailedDir) {
        List<String> ret = new ArrayList<>(fileArray.length);

        for (String file : fileArray) {
            ret.add(baseURL + CONTEXT + lastFailedDir + "/" + file);
        }

        return ret;
    }

    @Override
    public List<String> getFailedFiles(final String baseURL) {
        return listBuilder(baseURL, LAST_FAILED_DIR);
    }

    @Override
    public List<String> getSuccessFiles(final String baseURL) {
        return listBuilder(baseURL, LAST_SUCCESSFUL_DIR);
    }

    @Override
    public List<String> getTestFiles(final String baseURL, final String testNum) {
        return listBuilder(baseURL, testNum);
    }
}
