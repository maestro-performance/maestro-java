package org.maestro.worker.common.watchdog;

import org.maestro.worker.common.WorkerRuntimeInfo;
import org.maestro.worker.common.WorkerStaleChecker;

import java.util.List;

/**
 * An observer that checks if the workers have stopped sending data
 */
public class StaleObserver implements WatchdogObserver {
    private final WorkerStaleChecker workerStaleChecker;

    public StaleObserver(WorkerStaleChecker workerStaleChecker) {
        this.workerStaleChecker = workerStaleChecker;
    }

    @Override
    public boolean onStart() {
        workerStaleChecker.start();
        return true;
    }

    @Override
    public boolean onStop(List<WorkerRuntimeInfo> workerRuntimeInfos) {
        workerStaleChecker.stop();
        return true;
    }
}
