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

package org.maestro.worker.common;

import org.junit.Test;
import org.maestro.common.worker.WorkerStateInfo;

import static org.junit.Assert.*;

public class WorkerStateInfoUtilTest {
    @Test
    public void testCleanExitOnDefault() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        assertTrue(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNotNull(wsi.getExitStatus());
    }

    @Test
    public void testCleanExitOnStopped() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED, null);
        assertTrue(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNotNull(wsi.getExitStatus());
    }

    @Test
    public void testCleanExitOnSuccess() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS, null);
        assertTrue(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNotNull(wsi.getExitStatus());
    }

    @Test
    public void testFailedExitOnFailure() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, null);
        assertFalse(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNotNull(wsi.getExitStatus());
    }


    @Test
    public void testCleanExitOnStoppedRunning() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(true, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED, null);
        assertFalse(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNull(wsi.getExitStatus());
    }

    @Test
    public void testCleanExitOnSuccessRunning() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(true, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS, null);
        assertFalse(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNull(wsi.getExitStatus());
    }

    @Test
    public void testFailedExitOnFailureRunning() {
        WorkerStateInfo wsi = new WorkerStateInfo();

        wsi.setState(true, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, null);
        assertFalse(WorkerStateInfoUtil.isCleanExit(wsi));
        assertNull(wsi.getExitStatus());
    }
}
