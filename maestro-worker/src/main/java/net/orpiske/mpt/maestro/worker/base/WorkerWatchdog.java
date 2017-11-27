package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.client.MaestroReceiver;
import net.orpiske.mpt.common.worker.WorkerStateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The watchdog inspects the active workers to check whether they are still active, completed their job
 * or failed
 */
class WorkerWatchdog implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerWatchdog.class);

    private List<WorkerRuntimeInfo> workers;
    private MaestroReceiver endpoint;
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

    private boolean isCleanExit(WorkerStateInfo wsi) {
        if (wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS) {
            return true;
        }

        if (wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED) {
            return true;
        }

        return false;

    }

    @Override
    public void run() {
        logger.info("Running the worker watchdog");
        running = true;

        try {
            while (running && workersRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    break;
                }
            }


            //TODO check if notifyFailure should happen before or after waiting the workers to stop
            for (WorkerRuntimeInfo ri : workers) {
                WorkerStateInfo wsi = ri.worker.getWorkerState();

                if (!wsi.isRunning()) {
                    if (!isCleanExit(wsi)) {
                        endpoint.notifyFailure(wsi.getException().getMessage());

                        return;
                    }
                }
            }

        } finally {
            this.onWorkersStopped.accept(workers);
        }

        endpoint.notifySuccess("Test completed successfully");
        logger.info("Running the worker watchdog");
    }

    public boolean isRunning() {
        return running;
    }
}
