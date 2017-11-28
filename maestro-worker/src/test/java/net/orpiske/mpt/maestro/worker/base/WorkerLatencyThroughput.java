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

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.maestro.worker.jms.JMSReceiverWorker;
import net.orpiske.mpt.maestro.worker.jms.ReceiverClient;
import org.HdrHistogram.EncodableHistogram;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramLogReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

public class WorkerLatencyThroughput {

    private static final class DummyReceiverClient implements ReceiverClient {

        private final long intervalNanos;
        private long nextFireTime;
        private final long latencyMillis;

        DummyReceiverClient(long rate, long latencyMillis) {
            this.intervalNanos = rate > 0 ? 1000_000_000L / rate : 0;
            this.latencyMillis = latencyMillis;
        }


        private void waitUntilFireTime() {
            if (intervalNanos > 0) {
                long now;
                final long expectedTriggerTime = nextFireTime;
                do {
                    now = System.nanoTime();
                    final long waitNanos = expectedTriggerTime - now;
                    if (waitNanos > 0) {
                        LockSupport.parkNanos(waitNanos);
                    }
                } while (now - nextFireTime < 0);
                nextFireTime = expectedTriggerTime + intervalNanos;
            }
        }

        @Override
        public long receiveMessages() throws Exception {
            waitUntilFireTime();
            return System.currentTimeMillis() - latencyMillis;

        }

        @Override
        public void start() throws Exception {
            if (intervalNanos > 0) {
                nextFireTime = System.nanoTime() + intervalNanos;
            }
        }

        @Override
        public void stop() {
            //NO OP
        }

        @Override
        public void setUrl(String s) {
            //NO OP
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final boolean parseData = false;
        final long reportingIntervalMillis = 1000;
        final long latencyMillis = 100;
        final File reportFolder = new File("./");
        final int workers = 1;
        final long rate = 100;
        final Thread[] workerThreads = new Thread[workers];
        final MaestroWorker[] maestroWorkers = new MaestroWorker[workers];
        final WorkerOptions workerOptions = new WorkerOptions();
        workerOptions.setDuration(Long.toString(Long.MAX_VALUE));
        for (int i = 0; i < workers; i++) {
            final int workerIndex = i;
            final JMSReceiverWorker worker = new JMSReceiverWorker(() -> new DummyReceiverClient(rate, latencyMillis));
            worker.setWorkerOptions(workerOptions);
            maestroWorkers[workerIndex] = worker;
            workerThreads[i] = new Thread(worker);
            workerThreads[i].setDaemon(true);
            workerThreads[i].setName("worker-" + workerIndex);
        }
        final WorkerLatencyWriter channelWriter = reportingIntervalMillis > 0 ? new WorkerLatencyWriter(reportFolder, Arrays.asList(maestroWorkers), reportingIntervalMillis) : new WorkerLatencyWriter(reportFolder, Arrays.asList(maestroWorkers));
        final Thread writerThread = new Thread(channelWriter);
        writerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Stream.of(maestroWorkers).forEach(MaestroWorker::stop);
            Stream.of(workerThreads).forEach(workerThread -> {
                try {
                    workerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("FINISHED WRITES");
            }
            if (parseData) {
                try {
                    final HistogramLogReader logReader = new HistogramLogReader(new File(reportFolder, "receiverd-latency.hdr"));
                    int i = 0;
                    while (logReader.hasNext()) {
                        final EncodableHistogram encodableHistogram = logReader.nextIntervalHistogram();
                        if (encodableHistogram instanceof Histogram) {
                            final Histogram histogram = (Histogram) encodableHistogram;
                            System.out.println("******************************************");
                            System.out.println("HISTOGRAM " + (i + 1));
                            System.out.println("******************************************");
                            histogram.outputPercentileDistribution(System.out, 1d);
                            i++;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }));
        final Thread reporterThread = new Thread(() -> {
            final StringBuilder report = new StringBuilder();
            final long[] lastMessageCount = new long[workers];
            long lastCheck = System.currentTimeMillis();
            for (int i = 0; i < workers; i++) {
                lastMessageCount[i] = maestroWorkers[i].messageCount();
            }
            //print reports
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    continue;
                }
                report.setLength(0);
                long now = System.currentTimeMillis();
                final long intervalLength = now - lastCheck;
                report.append(" - ").append(intervalLength).append(" ms");
                for (int i = 0; i < workers; i++) {
                    final long transmitted = maestroWorkers[i].messageCount();
                    final long transmissedInterval = transmitted - lastMessageCount[i];
                    report.append(" - [").append(i).append("]\t").append(transmissedInterval);
                    lastMessageCount[i] = transmitted;
                }
                System.out.println(report);
                lastCheck = now;
            }
        });
        reporterThread.start();
        Stream.of(workerThreads).forEach(Thread::start);
    }
}
