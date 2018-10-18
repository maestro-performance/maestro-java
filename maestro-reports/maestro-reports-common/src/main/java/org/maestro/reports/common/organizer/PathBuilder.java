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

package org.maestro.reports.common.organizer;

import java.io.File;

/**
 * Report path builder utility
 */
public class PathBuilder {
    private PathBuilder() {};

    /**
     * Builds a report path
     * @param baseDir
     * @param testId
     * @param testNumber
     * @return
     */
    public static final String build(final String baseDir, int testId, int testNumber) {
        return baseDir + File.separator + "id" + File.separator + testId + File.separator + "number" + File.separator +
                testNumber + File.separator;
    }
}
