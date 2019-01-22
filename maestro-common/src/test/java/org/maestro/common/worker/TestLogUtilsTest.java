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

package org.maestro.common.worker;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/*
 * Note that the sample log directory tree in the resources does not use links because links are not supported on all
 * OSes.
 */
public class TestLogUtilsTest {
    File logDir;

    @Before
    public void setUp() {
        final String path = this.getClass().getResource("sample/logs").getPath();
        logDir = new File(path);
    }


    @Test
    public void testLogDir() {
        File zeroDir = TestLogUtils.testLogDir(logDir, 0);
        assertTrue(zeroDir.exists());
    }

    @Test
    public void findLastLogDir() {
        File dir = TestLogUtils.findLastLogDir(logDir);
        assertTrue(dir.exists());
        assertEquals("3", dir.getName());
    }

    @Test
    public void lastTestLogDir() {
        File dir = TestLogUtils.lastTestLogDir(logDir);
        assertTrue(dir.exists());
        assertEquals("last", dir.getName());
    }

    @Test
    public void lastFailedTestLogDir() {
        File dir = TestLogUtils.lastFailedTestLogDir(logDir);
        assertTrue(dir.exists());
        assertEquals("lastFailed", dir.getName());
    }

    @Test
    public void lastSuccessfulTestLogDir() {
        File dir = TestLogUtils.lastSuccessfulTestLogDir(logDir);
        assertTrue(dir.exists());
        assertEquals("lastSuccessful", dir.getName());
    }

    @Test
    public void anyTestLogDir() {
        File dir = TestLogUtils.anyTestLogDir(logDir, "2");
        assertTrue(dir.exists());
        assertEquals("2", dir.getName());
    }

    @Test
    public void testLogDirNum() {
        File dir = TestLogUtils.anyTestLogDir(logDir, "2");
        assertTrue(dir.exists());

        assertEquals(2, TestLogUtils.testLogDirNum(dir));
    }

    @Test
    public void nextTestLogDir() {
        File dir = null;

        try {
            dir = TestLogUtils.nextTestLogDir(logDir);

            // The directory must be created when calling the next test log dir
            assertTrue(dir.exists());
            assertEquals("4", dir.getName());
        }
        finally {
            if (dir != null) {
                dir.delete();
            }
        }
    }
}