package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.client.MaestroReceiver;
import net.orpiske.mpt.common.worker.WorkerStateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The watchdog inspects the active workers to check whether they are still active, completed their job
 * or failed
 */
class WorkerWatchdog implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerWatchdog.class);

    private Collection<WorkerRuntimeInfo> workers;
    private MaestroReceiver endpoint;
    private boolean running = false;


    /**
     * Constructor
     * @param workers A list of workers to inspect
     * @param endpoint The maestro endpoint that is to be notified of the worker status
     */
    public WorkerWatchdog(Collection<WorkerRuntimeInfo> workers, MaestroReceiver endpoint) {
        this.workers = workers;
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
        for (WorkerRuntimeInfo ri : workers) {
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

        while (running && workersRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

                break;
            }
        }


        for (WorkerRuntimeInfo ri : workers) {
            WorkerStateInfo wsi = ri.worker.getWorkerState();

            if (!wsi.isRunning()) {
                if (wsi.getExitStatus() != WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS) {
                    endpoint.notifyFailure(wsi.getException().getMessage());

                    return;
                }
            }
        }

        endpoint.notifySuccess("Test completed successfully");
        logger.info("Running the worker watchdog");
    }
}
