package org.maestro.worker.common;

import org.maestro.common.NonProgressingStaleChecker;
import org.maestro.common.StaleChecker;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Checks a group of workers to ensure they are not stale
 */
public class WorkerStaleChecker {
    private final List<? extends MaestroWorker> workers;
    private static final StaleChecker staleChecker = new NonProgressingStaleChecker(30);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /**
     * Constructor
     * @param workers group of workers to check
     */
    public WorkerStaleChecker(List<? extends MaestroWorker> workers) {
        this.workers = workers;
    }


    /**
     * Starts checking for staled workers
     */
    public void start() {
        executorService.scheduleAtFixedRate(this::staleCheck, 5, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops checking for staled workers
     */
    public void stop() {
        executorService.shutdown();
    }

    private void staleCheck() {
        long count = 0;

        for (MaestroWorker worker : workers) {
            if (worker.isRunning()) {
                count += worker.messageCount();
            }
        }

        if (staleChecker.isStale(count)) {
            workers.get(0).fail(new MaestroException("Worker has staled after sending %d messages", count));
        }
    }
}
