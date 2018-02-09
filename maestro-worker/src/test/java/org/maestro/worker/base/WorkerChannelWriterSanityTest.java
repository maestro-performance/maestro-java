/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.worker.base;

import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.worker.*;
import org.maestro.common.writers.OneToOneWorkerChannel;
import org.maestro.common.writers.RateWriter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

public class WorkerChannelWriterSanityTest {

    private static abstract class DummyWorker implements MaestroWorker {

        private final OneToOneWorkerChannel workerChannel;
        long startedEpochMillis = Long.MIN_VALUE;

        DummyWorker(int channelCapacity) {
            workerChannel = new OneToOneWorkerChannel(channelCapacity);
        }

        @Override
        public boolean isRunning() {
            throw new UnsupportedOperationException();
        }

        @Override
        public OneToOneWorkerChannel workerChannel() {
            return workerChannel;
        }

        @Override
        public void setWorkerOptions(WorkerOptions workerOptions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WorkerStateInfo getWorkerState() {
            return null;
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void halt() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long startedEpochMillis() {
            return startedEpochMillis;
        }

        @Override
        public long messageCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWorkerNumber(int number) {
            // NO-OP
        }
    }

    private static final class DummySenderWorker extends DummyWorker implements MaestroSenderWorker {

        DummySenderWorker(int channelCapacity) {
            super(channelCapacity);
        }
    }

    private static final class DummyReceiverWorker extends DummyWorker implements MaestroReceiverWorker {

        DummyReceiverWorker(int channelCapacity) {
            super(channelCapacity);
        }
    }

    @Rule
    public final TemporaryFolder tempTestFolder = new TemporaryFolder();

    @Test(timeout = 120_000L)
    public void shouldWriteRatesWithoutMissingSamplesIfNotFull() throws IOException, InterruptedException {
        final EpochMicroClock epochMicroClock = EpochClocks.exclusiveMicro();
        final File testReportFolder = tempTestFolder.newFolder("test_report");
        final long elapsedNanosToWrite;
        try (RateWriter rateWriter = new RateWriter(testReportFolder, true, true)) {
            final long startWrite = System.nanoTime();
            rateWriter.write(epochMicroClock.microTime(), epochMicroClock.microTime());
            elapsedNanosToWrite = System.nanoTime() - startWrite;
        }
        //estimation that takes into account a double writes (sender and writer) + not optimized code while writing
        final long intervalEmitNanos = elapsedNanosToWrite * 4;
        final AtomicBoolean forceStopWorker = new AtomicBoolean(false);
        final int events = 10;
        final long testTimeoutMillis = TimeUnit.MINUTES.toMillis(1);
        //there are 1 producer + 1 consumer each one emitting events
        final int totalEvents = events * 2;
        final CountDownLatch eventsProcessed = new CountDownLatch(totalEvents);
        //use 1 capacity and wait until each message has been processed
        final DummyReceiverWorker dummyReceiverWorker = new DummyReceiverWorker(1);
        final DummySenderWorker dummySenderWorker = new DummySenderWorker(1);
        final List<DummyWorker> dummyWorkers = new ArrayList<>();
        dummyWorkers.add(dummySenderWorker);
        dummyWorkers.add(dummyReceiverWorker);
        final File reportFolder = tempTestFolder.newFolder("report");
        final WorkerChannelWriter channelWriter = new WorkerChannelWriter(reportFolder, dummyWorkers);
        final Thread writerThread = new Thread(channelWriter);
        writerThread.setDaemon(true);
        writerThread.start();
        final Thread[] producers = new Thread[dummyWorkers.size()];
        for (int i = 0; i < producers.length; i++) {
            final DummyWorker dummyWorker = dummyWorkers.get(i);
            producers[i] = new Thread(() -> {
                final EpochMicroClock microClock = EpochClocks.exclusiveMicro();
                final OneToOneWorkerChannel workerChannel = dummyWorker.workerChannel;
                dummyWorker.startedEpochMillis = System.currentTimeMillis();
                for (int e = 0; e < events; e++) {
                    final long now = microClock.microTime();
                    workerChannel.emitRate(now, now);
                    //wait 1 second before considering the event processed
                    LockSupport.parkNanos(intervalEmitNanos);
                    if (forceStopWorker.get()) {
                        return;
                    }
                    eventsProcessed.countDown();
                }
            });
            producers[i].setDaemon(true);
        }
        Stream.of(producers).forEach(Thread::start);
        final boolean success = eventsProcessed.await(testTimeoutMillis, TimeUnit.MILLISECONDS);
        forceStopWorker.lazySet(true);
        Assert.assertTrue("Processed events: " + eventsProcessed.getCount() + "/" + totalEvents, success);
        writerThread.interrupt();
        writerThread.join();
        Assert.assertEquals("No missed samples with a synchronized writing rate", 0, dummyReceiverWorker.workerChannel().missedSamples() + dummySenderWorker.workerChannel().missedSamples());
    }
}
