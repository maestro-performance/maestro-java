/*
 * Copyright 2018 Otavio Rodolfo Piske
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
import static org.junit.Assert.*;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.worker.MaestroSenderWorker;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.common.worker.WorkerStateInfo;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class WorkerDataUtilsTest {

    private static class DummySender implements MaestroSenderWorker {
        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void setWorkerOptions(WorkerOptions workerOptions) {

        }

        @Override
        public WorkerStateInfo getWorkerState() {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void fail(Exception exception) {

        }

        @Override
        public void halt() {

        }

        @Override
        public void setWorkerNumber(int number) {

        }

        @Override
        public void run() {

        }

        @Override
        public long startedEpochMillis() {
            return 0;
        }

        @Override
        public long messageCount() {
            return 0;
        }

        @Override
        public void setupBarriers(CountDownLatch startSignal, CountDownLatch endSignal) {
            // no-op
        }
    }

    @Test
    public void testCreate() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportDir = new File(path);

        try (BinaryRateWriter writer = WorkerDataUtils.writer(reportDir, new DummySender())) {
            assertNotNull("The writer should not be null", writer);
            assertEquals("The report path does not match", new File(reportDir, "sender.dat"), writer.reportFile());
        }
    }
}
