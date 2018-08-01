package org.maestro.worker.common;

import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.worker.MaestroWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerRateWriter implements Runnable {
    private static class WriterCache {
        private long count;
        private BinaryRateWriter writer;

        public WriterCache(long count, BinaryRateWriter writer) {
            this.count = count;
            this.writer = writer;
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(WorkerRateWriter.class);
    private Map<Class<?>, WriterCache> cachedWriters = new HashMap<>(4);
    private List<? extends MaestroWorker> workers;
    private EpochMicroClock microClock = EpochClocks.exclusiveMicro();

    private boolean running = false;

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
        long currentTime = microClock.microTime();

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
            writeRecord(clazz, cache, currentCount, currentTime);
        }
    }

    private void writeRecord(Class<?> clazz, WriterCache cache, long currentCount, long currentTime) {
        BinaryRateWriter writer = cache.writer;

        try {
            long delta = currentCount - cache.count;

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

        while (running) {
            cachedWriters.forEach(this::updateForWorker);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.info("Rate writer was interrupted");
                break;
            }
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
