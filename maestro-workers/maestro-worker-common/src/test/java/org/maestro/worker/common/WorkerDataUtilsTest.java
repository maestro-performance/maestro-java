package org.maestro.worker.common;

import org.junit.Test;
import static org.junit.Assert.*;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.worker.MaestroSenderWorker;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.common.worker.WorkerStateInfo;

import java.io.File;
import java.io.IOException;

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
    }

    @Test
    public void testCreate() throws IOException {
        String path = this.getClass().getResource(".").getPath();
        File reportDir = new File(path);

        BinaryRateWriter writer = WorkerDataUtils.writer(reportDir, new DummySender());
        assertNotNull("The writer should not be null", writer);
        assertEquals("The report path does not match", new File(reportDir, "sender.dat"), writer.reportFile());
    }
}
