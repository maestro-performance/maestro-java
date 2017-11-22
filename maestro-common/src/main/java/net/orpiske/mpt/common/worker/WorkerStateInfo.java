package net.orpiske.mpt.common.worker;

/**
 * Holds the worker state information
 */
public final class WorkerStateInfo {

    /**
     * The exit status for the worker
     */
    public enum WorkerExitStatus {
        /**
         * Worker exited successfully
         */
        WORKER_EXIT_SUCCESS,

        /**
         * Worker exited with failure
         */
        WORKER_EXIT_FAILURE,

        /**
         * Worker exited because it received stop request
         */
        WORKER_EXIT_STOPPED
    };

    private boolean running = false;

    private WorkerExitStatus exitStatus = WorkerExitStatus.WORKER_EXIT_STOPPED;
    private Exception exception;

    /**
     * Whether the worker is running
     * @return true if it's running or false otherwise
     */
    public boolean isRunning() {
        return running;
    }


    /**
     * Sets the running state for the worker
     * @param running true if running or false otherwise
     */
    public void setRunning(boolean running) {
        this.running = running;
    }


    /**
     * Gets the exit status for the worker
     * @return The exit status {@link WorkerExitStatus}
     */
    public WorkerExitStatus getExitStatus() {
        return exitStatus;
    }


    /**
     * Sets the exit status for the worker
     * @param exitStatus the worker status {@link WorkerExitStatus} (returns STOPPED if uninitialized)
     */
    public void setExitStatus(WorkerExitStatus exitStatus) {
        this.exitStatus = exitStatus;
    }


    /**
     * Gets the exception raised by the worker if it exited with failure
     * @return An exception object or null if none
     */
    public Exception getException() {
        return exception;
    }


    /**
     * Sets the exception raised by the worker
     * @param exception the exception raised by the worker
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }
}
