package org.maestro.worker.base;

import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.worker.WorkerStateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.maestro.worker.base.WorkerStateInfoUtil.isCleanExit;

/**
 * The watchdog inspects the active workers to check whether they are still active, completed their job
 * or failed
 */
class WorkerWatchdog implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerWatchdog.class);

    private final List<WorkerRuntimeInfo> workers;
    private final MaestroReceiver endpoint;
    private volatile boolean running = false;
    private final Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped;


    /**
     * Constructor
     * @param workers A list of workers to inspect
     * @param endpoint The maestro endpoint that is to be notified of the worker status
     */
    public WorkerWatchdog(List<WorkerRuntimeInfo> workers, MaestroReceiver endpoint, Consumer<? super List<WorkerRuntimeInfo>> onWorkersStopped) {
        this.workers = new ArrayList<>(workers);
        this.onWorkersStopped = onWorkersStopped;
        this.endpoint = endpoint;
    }


    /**
     * Sets the running state for the watchdog
     * @param running true if running or false otherwise
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean workersRunning() {
        for (int i = 0, size = workers.size(); i < size; i++) {
            WorkerRuntimeInfo ri = workers.get(i);
            if (!ri.thread.isAlive()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void run() {
        logger.info("Running the worker watchdog");
        running = true;
        boolean successful = true;
        String exceptionMessage = null;

        try {
            while (running && workersRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("The worker thread was interrupted", e);

                    break;
                }
            }


            // NOTE: flushing the data and creating the symlinks must happen before
            // the notification. Otherwise, the front-end risk collecting outdated
            // records/logs
            for (WorkerRuntimeInfo ri : workers) {
                WorkerStateInfo wsi = ri.worker.getWorkerState();

                if (!wsi.isRunning()) {
                    if (!isCleanExit(wsi)) {
                        successful = false;
                        exceptionMessage = Objects.requireNonNull(wsi.getException()).getMessage();

                        break;
                    }
                }
            }
        } finally {
            logger.debug("Waiting for flushing workers's data");
            this.onWorkersStopped.accept(workers);

            if (successful) {
                endpoint.notifySuccess("Test completed successfully");
            }
            else {
                if (exceptionMessage != null) {
                    endpoint.notifyFailure(exceptionMessage);
                }
                else {
                    endpoint.notifyFailure("Unhandled worker error");
                }
            }
        }

        logger.info("Finished running the worker watchdog");
    }

    public boolean isRunning() {
        return running;
    }
}
