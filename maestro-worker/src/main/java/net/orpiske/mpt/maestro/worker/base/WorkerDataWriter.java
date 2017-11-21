package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorkerDataWriter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerDataWriter.class);

    private final Collection<MaestroWorker> workers;
    private final long reportingIntervalMs = TimeUnit.SECONDS.toMillis(10);
    private final boolean reportIntervalLatencies = false;

    private static final class WorkerIntervalReport implements AutoCloseable {
        private final MaestroWorker worker;
        private final LatencyWriter latencyWriter;
        private final RateWriter rateWriter;
        private long lastMessageCount;
        //the reporting interval is [previousReportTime, lastReportTime]
        private long lastReportTime;
        private long previousReportTime;
        private long messagesInTheInterval;
        private Histogram intervalHistogram;
        private boolean reportIntervalLatencies;
        private final long startReportingTime;

        public static WorkerIntervalReport with(File latencies, File rates, MaestroWorker worker, boolean reportIntervalLatencies) {
            try {
                return new WorkerIntervalReport(
                        new LatencyWriter(latencies),
                        new RateWriter(rates),
                        worker, reportIntervalLatencies);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private WorkerIntervalReport(LatencyWriter latencyWriter, RateWriter rateWriter, MaestroWorker worker, boolean reportIntervalLatencies) {
            this.latencyWriter = latencyWriter;
            this.rateWriter = rateWriter;
            this.worker = worker;
            if (!reportIntervalLatencies) {
                this.intervalHistogram = null;
            } else {
                this.intervalHistogram = new Histogram(TimeUnit.HOURS.toMillis(1), 3);
            }
            //We can't be sure the worker is already up & running
            final long startedWorkerTime = worker.startedEpochMillis();
            this.lastReportTime = startedWorkerTime < 0 ? System.currentTimeMillis() : startedWorkerTime;
            this.lastMessageCount = worker.messageCount();
            this.startReportingTime = this.lastReportTime;
            this.previousReportTime = this.lastReportTime;
            this.messagesInTheInterval = 0;
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
            final long messageCount = this.worker.messageCount();
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
            this.previousReportTime = this.lastReportTime;
            this.lastReportTime = reportTime;
            this.messagesInTheInterval = messageCount - this.lastMessageCount;
            this.lastMessageCount = messageCount;
        }

        public void outputReport() {
            //TODO output the report somehow (using the 2 writers)
        }

        @Override
        public void close() {
            try {
                this.rateWriter.close();
            } finally {
                this.latencyWriter.close();
            }
        }
    }

    public WorkerDataWriter(Collection<MaestroWorker> workers) {
        this.workers = workers;
    }

    private static long getCurrentTimeMsecWithDelay(final long nextReportingTime) throws InterruptedException {
        final long now = System.currentTimeMillis();
        if (now < nextReportingTime)
            Thread.sleep(nextReportingTime - now);
        return now;
    }

    @Override
    public void run() {
        final List<WorkerIntervalReport> workerReports = this.workers.stream()
                .map(w -> WorkerIntervalReport.with(null, null, w, reportIntervalLatencies))
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
            workerReports.forEach(WorkerIntervalReport::close);
        }
    }
}
