/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.common.worker;

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
    }

    private volatile boolean running = false;
    private volatile WorkerExitStatus exitStatus = WorkerExitStatus.WORKER_EXIT_STOPPED;
    private volatile Exception exception = null;

    /**
     * Whether the worker is running
     * @return true if it's running or false otherwise
     */
    public boolean isRunning() {
        return running;
    }


    /**
     * Sets the state for the worker
     *
     * @param running true if the worker is in a running state
     * @param exitStatus the exit status for the worker {@link WorkerExitStatus}
     * @param exception Any exception raised in case of errors
     */
    public synchronized void setState(boolean running, WorkerExitStatus exitStatus, Exception exception) {
        this.exitStatus = exitStatus;
        this.exception = exception;
        //uses the this.running store to write release this.exitStatus and this.exception
        this.running = running;
    }


    /**
     * Gets the exit status for the worker
     * @return The exit status or null if running
     */
    public WorkerExitStatus getExitStatus() {
        //uses the this.running load to read acquire this.exitStatus
        if (this.running) {
            return null;
        }
        return exitStatus;
    }


    /**
     * Gets the exception raised by the worker if it exited with failure
     * @return An exception object or null if none
     */
    public Exception getException() {
        //uses the this.running load to read acquire this.exception
        if (this.running) {
            return null;
        }
        return exception;
    }
}
