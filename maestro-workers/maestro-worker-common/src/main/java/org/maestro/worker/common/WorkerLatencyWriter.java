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

import org.HdrHistogram.Histogram;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.io.data.writers.LatencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class WorkerLatencyWriter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerLatencyWriter.class);

    private static final class WorkerIntervalReport {
        private final MaestroWorker worker;
        private final LatencyWriter latencyWriter;
        private long lastReportTime;
        private Histogram intervalHistogram;
        private final boolean reportIntervalLatencies;
        private final long startReportingTime;
        private final LatencyEvaluator latencyEvaluator;

        WorkerIntervalReport(LatencyWriter latencyWriter, MaestroWorker worker, boolean reportIntervalLatencies,
                             long globalStartReportingTime, LatencyEvaluator latencyEvaluator) {
            this.latencyWriter = latencyWriter;
            this.worker = worker;
            this.intervalHistogram = null;
            //We can't be sure the worker is already up & running
            final long startedWorkerTime = worker.startedEpochMillis();
            //to avoid having a global start time > of the start time of a writer's interval latencies
            this.lastReportTime = Math.max(globalStartReportingTime, startedWorkerTime < 0 ? System.currentTimeMillis() : startedWorkerTime);
            this.startReportingTime = this.lastReportTime;
            this.reportIntervalLatencies = reportIntervalLatencies;
            this.latencyEvaluator = latencyEvaluator;
        }

        void updateReport() {
            updateReport(false);

            // Latency evaluation is optional
            if (this.latencyEvaluator != null) {
                logger.trace("Recording latency ...");
                this.latencyEvaluator.record(this.intervalHistogram);
            }
            else {
                logger.trace("No latency evaluator was set, ignoring ...");
            }
        }

        /**
         * @param snapshotLatencies {@code true} if is needed to force the snapshot of the interval latencies at the end of a test
         */
        void updateReport(final boolean snapshotLatencies) {
            final long reportTime = System.currentTimeMillis();
            if (snapshotLatencies || this.reportIntervalLatencies) {
                final Histogram intervalHistogram = this.worker.takeLatenciesSnapshot(this.intervalHistogram);
                //there are workers that doesn't support taking latencies histograms
                if (intervalHistogram != null) {
                    //the first time the startTimeStamp is the first one: useful when aren't performed
                    //snapshots of interval latencies
                    if (this.intervalHistogram == null) {
                        intervalHistogram.setStartTimeStamp(this.startReportingTime);
                    } else {
                        //if it is the first snapshot taken then it is from the beginning of the worker lifecycle
                        intervalHistogram.setStartTimeStamp(this.lastReportTime);
                    }
                    intervalHistogram.setEndTimeStamp(reportTime);
                    this.intervalHistogram = intervalHistogram;
                }
            }
            this.lastReportTime = reportTime;
        }

        void outputReport() {
            if (this.intervalHistogram != null && this.intervalHistogram.getTotalCount() > 0) {
                this.latencyWriter.outputIntervalHistogram(this.intervalHistogram);
            }
        }
    }

    private final List<? extends MaestroWorker> workers;
    private final File reportFolder;
    private final long reportingIntervalMs;
    private final boolean reportIntervalLatencies;
    private LatencyEvaluator latencyEvaluator;


    public WorkerLatencyWriter(File reportFolder, List<? extends MaestroWorker> workers) {
        this.reportFolder = reportFolder;
        this.workers = workers;
        //the first sleep will be a very long one :)
        this.reportingIntervalMs = TimeUnit.DAYS.toMillis(365);
        this.reportIntervalLatencies = false;
    }

    public WorkerLatencyWriter(File reportFolder, List<? extends MaestroWorker> workers,
                               final LatencyEvaluator latencyEvaluator, long reportingIntervalMs) {
        this.reportFolder = reportFolder;
        this.workers = workers;
        this.latencyEvaluator = latencyEvaluator;
        this.reportingIntervalMs = reportingIntervalMs;
        this.reportIntervalLatencies = true;
    }

    private static long getCurrentTimeMsecWithDelay(final long nextReportingTime) throws InterruptedException {
        final long now = System.currentTimeMillis();
        if (now < nextReportingTime)
            Thread.sleep(nextReportingTime - now);
        return now;
    }

    @Override
    public void run() {
        logger.debug("Updating latency information every {} milliseconds", reportingIntervalMs);

        final long anyWorkers = this.workers.stream()
                .filter(w -> w instanceof MaestroReceiverWorker).count();
        //avoid creating any file if there aren't  any MaestroReceiverWorker
        if (anyWorkers == 0) {
            return;
        }

        try (LatencyWriter latencyWriter = new LatencyWriter(new File(reportFolder, "receiverd-latency.hdr"))) {
            final long globalStartReportingTime = System.currentTimeMillis();
            latencyWriter.outputLegend(globalStartReportingTime);
            //TODO collect only receiver worker latencies: make it configurable or available on the MaestroWorker API
            final List<WorkerIntervalReport> workerReports = this.workers.stream()
                    .filter(w -> w instanceof MaestroReceiverWorker).map(w ->
                            new WorkerIntervalReport(latencyWriter, w, reportIntervalLatencies, globalStartReportingTime, latencyEvaluator))
                    .collect(Collectors.toList());
            final Thread currentThread = Thread.currentThread();
            long startTime = System.currentTimeMillis();
            long nextReportingTime = startTime + reportingIntervalMs;

            try {
                while (!currentThread.isInterrupted()) {
                    final long now = getCurrentTimeMsecWithDelay(nextReportingTime);

                    if (now >= nextReportingTime) {
                        //the overall update + output process could take more than the reportingIntervalMs
                        //sample
                        workerReports.forEach(WorkerIntervalReport::updateReport);
                        //output sample
                        workerReports.forEach(WorkerIntervalReport::outputReport);
                        //move the new reporting time n reportingIntervalMs > now
                        while (now >= nextReportingTime) {
                            nextReportingTime += reportingIntervalMs;
                        }
                    }
                }
            } catch (InterruptedException i) {
                //it is legal
            } finally {
                //force a final snapshot of the latencies
                workerReports.forEach(r -> r.updateReport(true));
                workerReports.forEach(WorkerIntervalReport::outputReport);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
