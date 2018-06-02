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

import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.MaestroSenderWorker;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.writers.OneToOneWorkerChannel;
import org.maestro.common.writers.RateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class WorkerChannelWriter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerChannelWriter.class);

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
            this.rateWriter.write(rate.timestampEpochMicros(), rate.value());
        }

        public int updateReport(int drainLimit) {
            return this.worker.workerChannel().readRate(this.onRate, drainLimit);
        }
    }

    private final List<? extends MaestroWorker> workers;
    private final File reportFolder;
    private final boolean compressed;

    public WorkerChannelWriter(File reportFolder, List<? extends MaestroWorker> workers) {
        this.reportFolder = reportFolder;
        this.workers = new ArrayList<>(workers);
        this.compressed = true;
    }

    private RateWriter createRateWriter(boolean sender) throws IOException {
        return new RateWriter(reportFolder, sender, compressed);
    }


    @Override
    public void run() {

        RateWriter rateWriter = null;

        try {
            final int workersCount = workers.size();
            final List<WorkerRateReport> rateReports = new ArrayList<>(workersCount);

            for (int workerId = 0; workerId < workersCount; workerId++) {
                final MaestroWorker worker = workers.get(workerId);

                if (worker.workerChannel() != null) {
                    final boolean sender = worker instanceof MaestroSenderWorker;
                    final boolean receiver = worker instanceof MaestroReceiverWorker;
                    assert !(sender && receiver);

                    if (sender) {
                        rateWriter = rateWriter == null ? createRateWriter(true) : rateWriter;
                    } else if (receiver) {
                        rateWriter = rateWriter == null ? createRateWriter(false) : rateWriter;
                    }

                    if (rateWriter != null) {
                        rateReports.add(new WorkerRateReport(worker, rateWriter));
                    }
                }
            }

            update(rateReports);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //close the report writers
            try {
                if (rateWriter != null) {
                    rateWriter.close();
                }
            } finally {
                final long totalMissed = workers.stream().filter(w -> w.workerChannel() != null).map(MaestroWorker::workerChannel).mapToLong(OneToOneWorkerChannel::missedSamples).sum();
                if (totalMissed > 0) {
                    logger.error("Total missed rate samples: {}", totalMissed);
                }
            }
        }
    }

    public void update(List<WorkerRateReport> rateReports) {
        final int drainLimit = 128;

        final int rateReportsCount = rateReports.size();
        //doesn't need to continue if there aren't any reports to be populated
        if (rateReportsCount > 0) {
            final Thread currentThread = Thread.currentThread();
            final IdleStrategy idleStrategy = new SleepingIdleStrategy(1000L);
            while (!currentThread.isInterrupted()) {
                int events = 0;
                for (int i = 0; i < rateReportsCount; i++) {
                    final WorkerRateReport report = rateReports.get(i);
                    events += report.updateReport(drainLimit);
                }
                idleStrategy.idle(events);
            }

            drain(drainLimit, rateReports, rateReportsCount);

        }
    }

    public void drain(int drainLimit, List<WorkerRateReport> rateReports, int rateReportsCount) {
        //lets finish to drain the remaining samples left (if any)
        boolean allDrained = false;
        while (!allDrained) {
            allDrained = true;
            for (int i = 0; i < rateReportsCount; i++) {
                final WorkerRateReport report = rateReports.get(i);
                if (report.updateReport(drainLimit) > 0) {
                    allDrained = false;
                }
            }
        }
    }
}
