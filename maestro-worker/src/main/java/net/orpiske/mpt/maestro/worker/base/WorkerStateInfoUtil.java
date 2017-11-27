package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.WorkerStateInfo;


class WorkerStateInfoUtil {

    /**
     * Utility to check if the workers exited cleanly or not
     */
    public static boolean isCleanExit(WorkerStateInfo wsi) {
        if (wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS) {
            return true;
        }

        if (wsi.getExitStatus() == WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED) {
            return true;
        }

        return false;
    }

}
