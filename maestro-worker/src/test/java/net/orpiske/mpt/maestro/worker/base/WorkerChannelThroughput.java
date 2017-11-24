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

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

public class WorkerChannelThroughput {

    public static void main(String[] args) throws InterruptedException {
        final int capacity = 128 * 1024;
        final File reportFolder = new File("./");
        final int workers = 2;
        final long rate = 0;
        final long intervalNanos = rate > 0 ? (1_000_000_000L / rate) : 0;
        final Thread[] workerThreads = new Thread[workers];
        final MaestroWorker[] maestroWorkers = new MaestroWorker[workers];
        final AtomicLong[] transmittedEvents = new AtomicLong[workers];
        for (int i = 0; i < workers; i++) {
            final AtomicLong transmitted = new AtomicLong(0);
            final int workerIndex = i;
            final WorkerChannelWriterSanityTest.DummyWorker worker = new WorkerChannelWriterSanityTest.DummySenderWorker(capacity);
            maestroWorkers[workerIndex] = worker;
            transmittedEvents[workerIndex] = transmitted;
            workerThreads[i] = new Thread(() -> {
                final long interval = intervalNanos;
                long count = 1;
                //any worker is good
                long nextFire = System.nanoTime() + interval;
                while (!Thread.currentThread().isInterrupted()) {
                    if (interval > 0) {
                        //wait until time arrive
                        final long now = System.nanoTime();
                        final long expectedTriggerTime = nextFire;
                        final long waitNanos = expectedTriggerTime - now;
                        if (waitNanos > 0) {
                            //TODO warns if waitNanos is below the precision offered by the OS
                            LockSupport.parkNanos(waitNanos);
                        }
                        nextFire += interval;
                    }
                    final long timestamp = System.currentTimeMillis();
                    worker.workerChannel().emitRate(timestamp, timestamp);
                    count++;
                    transmitted.lazySet(count);
                }
            });
            workerThreads[i].setDaemon(true);
            workerThreads[i].setName("worker-" + workerIndex);
        }
        System.out.println("Estimated footprint of buffering is: " + (Stream.of(maestroWorkers).mapToLong(w -> w.workerChannel().footprintInBytes()).sum() / 1024) + " KB");
        final WorkerChannelWriter channelWriter = new WorkerChannelWriter(reportFolder, Arrays.asList(maestroWorkers));
        final Thread writerThread = new Thread(channelWriter);
        writerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Stream.of(workerThreads).forEach(Thread::interrupt);
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
        final Thread reporterThread = new Thread(() -> {
            final StringBuilder report = new StringBuilder();
            final long[] lastMessageCount = new long[workers];
            long lastCheck = System.currentTimeMillis();
            final long[] lastMissedCount = new long[workers];
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
                    final long transmitted = transmittedEvents[i].get();
                    final long missed = maestroWorkers[i].workerChannel().missedSamples();
                    final long transmissedInterval = transmitted - lastMessageCount[i];
                    final long missedInInterval = missed - lastMissedCount[i];
                    report.append(" - [").append(i).append("]\t").append(transmissedInterval).append(" missed= ").append(missedInInterval);
                    lastMessageCount[i] = transmitted;
                    lastMissedCount[i] = missed;
                }
                System.out.println(report);
                lastCheck = now;
            }
        });
        reporterThread.start();
        Stream.of(workerThreads).forEach(Thread::start);
    }
}
