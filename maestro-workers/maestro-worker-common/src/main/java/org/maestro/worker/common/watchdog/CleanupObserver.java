package org.maestro.worker.common.watchdog;

import org.maestro.worker.common.WorkerRuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Cleans up the list of workers and do any other cleanup required
 */
public class CleanupObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(CleanupObserver.class);

    @Override
    public boolean onStop(List<WorkerRuntimeInfo> workerRuntimeInfos) {
        logger.info("Cleaning up the list of worker runtimes");

        workerRuntimeInfos.clear();
        return false;
    }
}
