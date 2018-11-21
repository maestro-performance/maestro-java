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

package org.maestro.reports.server.collector;

import org.maestro.common.client.notes.Test;
import org.maestro.common.worker.TestLogUtils;

import java.io.File;

public class TestDirectoryUtils {

    private static File getTestDirectory(final File baseDir, int num) {
        File testDir;

        if (num == Test.NEXT) {
            testDir = TestLogUtils.nextTestLogDir(baseDir);
        }
        else {
            if (num == Test.LAST) {
                testDir = TestLogUtils.findLastLogDir(baseDir);
            }
            else {
                testDir = TestLogUtils.testLogDir(baseDir, num);
            }
        }

        return testDir;
    }

    public static File getTestDirectory(int requestedTestNumber, final File baseDir) {
        return getTestDirectory(baseDir, requestedTestNumber);
    }

    @Deprecated
    public static File getTestDirectory(final Test requestedTest, final File baseDir) {
        int requestedTestNumber = requestedTest.getTestNumber();

        return getTestDirectory(requestedTestNumber, baseDir);
    }

    public static File getTestIterationDirectory(int requestedTestIteration, final File baseDir) {
        return getTestDirectory(baseDir, requestedTestIteration);
    }

    @Deprecated
    public static File getTestIterationDirectory(final Test requestedTest, final File baseDir) {
        int requestedTestIteration = requestedTest.getTestIteration();

        return getTestDirectory(requestedTestIteration, baseDir);
    }
}
