/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.writers.OneToOneWorkerChannel;
import net.orpiske.mpt.common.writers.RateWriter;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class WorkerChannelWriter implements Runnable {

    private static final class WorkerRateReport {

        private final RateWriter rateWriter;
        private final MaestroWorker worker;
        private final Consumer<OneToOneWorkerChannel.Sample> onRate;

        public WorkerRateReport(MaestroWorker worker, RateWriter rateWriter) {
            this.rateWriter = rateWriter;
            this.worker = worker;
            this.onRate = this::onRate;
        }

        private void onRate(OneToOneWorkerChannel.Sample rate) {
            this.rateWriter.write(rate.timestampEpochMillis(), rate.value());
        }

        public int updateReport() {
            return this.worker.workerChannel().readRate(this.onRate, Integer.MAX_VALUE);
        }
    }

    private final List<MaestroWorker> workers;
    private final File reportFolder;
    private final boolean compressed;

    public WorkerChannelWriter(File reportFolder, List<MaestroWorker> workers) {
        this.reportFolder = reportFolder;
        this.workers = workers;
        this.compressed = true;
    }

    @Override
    public void run() {
        try (RateWriter senderRateWriter = new RateWriter(reportFolder, true, compressed);
             RateWriter receiverRateWriter = new RateWriter(reportFolder, false, compressed)) {
            final int workersCount = workers.size();
            final List<WorkerRateReport> rateReports = new ArrayList<>(workersCount);
            for (int workerId = 0; workerId < workersCount; workerId++) {
                final MaestroWorker worker = workers.get(workerId);
                final boolean sender = worker instanceof MaestroSenderWorker;
                final boolean receiver = worker instanceof MaestroReceiverWorker;
                assert sender == true && receiver == true;
                RateWriter rateWriter = null;
                if (sender) {
                    rateWriter = senderRateWriter;
                } else if (receiver) {
                    rateWriter = receiverRateWriter;
                }
                if (rateWriter != null) {
                    rateReports.add(new WorkerRateReport(worker, rateWriter));
                }
            }
            final int rateReportsCount = rateReports.size();
            final Thread currentThread = Thread.currentThread();
            final IdleStrategy idleStrategy = new SleepingIdleStrategy(1000L);
            while (!currentThread.isInterrupted()) {
                int events = 0;
                for (int i = 0; i < rateReportsCount; i++) {
                    final WorkerRateReport report = rateReports.get(i);
                    events += report.updateReport();
                }
                idleStrategy.idle(events);
            }
            //lets finish to drain the remaining samples left (if any)
            boolean allDrained = false;
            while (!allDrained) {
                allDrained = true;
                for (int i = 0; i < rateReportsCount; i++) {
                    final WorkerRateReport report = rateReports.get(i);
                    if (report.updateReport() > 0) {
                        allDrained = false;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
