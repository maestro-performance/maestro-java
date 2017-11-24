package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.writers.LatencyWriter;
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
        private boolean reportIntervalLatencies;
        private final long startReportingTime;

        public WorkerIntervalReport(LatencyWriter latencyWriter, MaestroWorker worker, boolean reportIntervalLatencies) {
            this.latencyWriter = latencyWriter;
            this.worker = worker;
            if (!reportIntervalLatencies) {
                this.intervalHistogram = null;
            } else {
                this.intervalHistogram = new Histogram(TimeUnit.HOURS.toMillis(1), 3);
            }
            //We can't be sure the worker is already up & running
            final long startedWorkerTime = worker.startedEpochMillis();
            this.lastReportTime = startedWorkerTime < 0 ? System.currentTimeMillis() : startedWorkerTime;
            this.startReportingTime = this.lastReportTime;
            this.reportIntervalLatencies = false;
        }

        public void updateReport() {
            updateReport(false);
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
    //TODO make it configurable
    private final long reportingIntervalMs = TimeUnit.SECONDS.toMillis(10);
    private final boolean reportIntervalLatencies = false;


    public WorkerLatencyWriter(File reportFolder, List<? extends MaestroWorker> workers) {
        this.reportFolder = reportFolder;
        this.workers = new ArrayList<>(workers);
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
                latencyWriter.outputLegend(System.currentTimeMillis());
                //TODO collect only receiver worker latencies: make it configurable or available on the MaestroWorker API
                final List<WorkerIntervalReport> workerReports = this.workers.stream()
                        .filter(w -> w instanceof MaestroReceiverWorker).map(w -> new WorkerIntervalReport(latencyWriter, w, reportIntervalLatencies))
                        .collect(Collectors.toList());
                final Thread currentThread = Thread.currentThread();
                long startTime = System.currentTimeMillis();
                long nextReportingTime = startTime + reportingIntervalMs;
                try {
                    while (!currentThread.isInterrupted()) {
                        final long now = getCurrentTimeMsecWithDelay(nextReportingTime);
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
