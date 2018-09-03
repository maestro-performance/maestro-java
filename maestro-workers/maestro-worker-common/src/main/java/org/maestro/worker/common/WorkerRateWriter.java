/*
 * Copyright 2018 Otavio Rodolfo Piske
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

import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.WorkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkerRateWriter implements Runnable {
    private static class WriterCache {
        private long count;
        private final BinaryRateWriter writer;

        WriterCache(long count, BinaryRateWriter writer) {
            this.count = count;
            this.writer = writer;
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(WorkerRateWriter.class);
    private final Map<Class<?>, WriterCache> cachedWriters = new HashMap<>(4);
    private final List<? extends MaestroWorker> workers;
    private final EpochMicroClock microClock = EpochClocks.exclusiveMicro();

    private volatile boolean running = false;

    public WorkerRateWriter(final File reportFolder, final List<? extends MaestroWorker> workers) throws IOException  {
        for (MaestroWorker worker : workers) {
            WriterCache cache = cachedWriters.get(worker.getClass());

            if (cache == null) {
                BinaryRateWriter writer = WorkerDataUtils.writer(reportFolder, worker);
                cache = new WriterCache(0, writer);
                cachedWriters.put(worker.getClass(), cache);
            }
        }

        this.workers = workers;
    }

    private void updateForWorker(Class<?> clazz, WriterCache cache) {
        long currentCount = 0;
        boolean stopped = false;

        for (MaestroWorker worker : workers) {
            if (worker.getClass() == clazz) {
                if (worker.isRunning()) {
                    currentCount += worker.messageCount();
                }
                else {
                    stopped = true;
                }
            }
        }

        if (!stopped) {
            writeRecord(clazz, cache, currentCount);
        }
    }

    private void writeRecord(Class<?> clazz, WriterCache cache, long currentCount) {
        BinaryRateWriter writer = cache.writer;

        try {
            long delta = currentCount - cache.count;

            final long currentTime = microClock.microTime();
            writer.write(0, delta, currentTime);

            cache.count = currentCount;
        } catch (IOException e) {
            logger.error("Unable to record the rate entry for worker class {}: {}", clazz, e.getMessage(), e);

            MaestroWorker worker = workers.get(0);

            worker.fail(new MaestroException("Unable to record the rate entry", e));
        }
    }

    @Override
    public void run() {
        running = true;

        final long interval = 1_000_000_000L;
        long nextFireTime = System.nanoTime() + interval;

        while (running) {
            final long now = WorkerUtils.waitNanoInterval(nextFireTime, interval);

            final long drift = TimeUnit.NANOSECONDS.toMillis(now - nextFireTime);

            if (drift > 0) {
                logger.warn("The current time is {} milliseconds beyond the scheduled time of check. The writer is probably " +
                        "unable update all the data within the scheduled time.",
                        drift);
            }

            nextFireTime += interval;
            cachedWriters.forEach(this::updateForWorker);
        }

        cachedWriters.values().forEach(writerCache -> writerCache.writer.close());
    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
