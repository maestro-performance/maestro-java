package org.maestro.worker.base;

import org.maestro.common.worker.WorkerStateInfo;


class WorkerStateInfoUtil {

    /**
     * Utility to check if the workers exited cleanly or not
     */
    public static boolean isCleanExit(WorkerStateInfo wsi) {
        if (wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS) {
            return true;
        }

        return wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED;

    }

}
