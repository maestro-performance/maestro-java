package org.maestro.worker.base;

import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.writers.LatencyWriter;
import org.HdrHistogram.Histogram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class WorkerLatencyWriter implements Runnable {

    private static final class WorkerIntervalReport {
        private final MaestroWorker worker;
        private final LatencyWriter latencyWriter;
        private long lastReportTime;
        private Histogram intervalHistogram;
        private final boolean reportIntervalLatencies;
        private final long startReportingTime;
        private final LatencyEvaluator latencyEvaluator;

        public WorkerIntervalReport(LatencyWriter latencyWriter, MaestroWorker worker, boolean reportIntervalLatencies,
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

        public void updateReport() {
            updateReport(false);

            // Latency evaluation is optional
            if (this.latencyEvaluator != null) {
                this.latencyEvaluator.record(this.intervalHistogram);
            }
        }

        /**
         * @param snapshotLatencies {@code true} if is needed to force the snapshot of the interval latencies at the end of a test
         */
        public void updateReport(final boolean snapshotLatencies) {
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

        public void outputReport() {
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


    public WorkerLatencyWriter(File reportFolder, List<? extends MaestroWorker> workers,
                               final LatencyEvaluator latencyEvaluator) {
        this.reportFolder = reportFolder;
        this.workers = new ArrayList<>(workers);
        //the first sleep will be a very long one :)
        this.reportingIntervalMs = TimeUnit.DAYS.toMillis(365);
        this.reportIntervalLatencies = false;
        this.latencyEvaluator = latencyEvaluator;
    }

    public WorkerLatencyWriter(File reportFolder, List<? extends MaestroWorker> workers, long reportingIntervalMs) {
        this.reportFolder = reportFolder;
        this.workers = new ArrayList<>(workers);
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
        final long anyWorkers = this.workers.stream()
                .filter(w -> w instanceof MaestroReceiverWorker).count();
        //avoid creating any file if there aren't  any MaestroReceiverWorker
        if (anyWorkers > 0) {
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
}
