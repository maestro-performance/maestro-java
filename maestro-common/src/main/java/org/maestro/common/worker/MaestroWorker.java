/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.worker;

import org.maestro.common.duration.TestDuration;
import org.maestro.common.writers.OneToOneWorkerChannel;
import org.HdrHistogram.Histogram;

/**
 * A common interface for any type of Maestro worker.
 */
public interface MaestroWorker extends Runnable, TestDuration.TestProgress {

    /**
     * Checks whether the worker is in running state.
     *
     * @return true if it is in running state or false otherwise
     */
    boolean isRunning();


    /**
     * Sets the options for this worker
     *
     * @param workerOptions the worker options to set
     */
    void setWorkerOptions(WorkerOptions workerOptions);


    /**
     * Gets the current state of the worker
     *
     * @return the current state of the worker
     */
    WorkerStateInfo getWorkerState();


    /**
     * Starts the test
     */
    void start();


    /**
     * Stops the test execution
     */
    void stop();


    /**
     * Halt the daemons
     */
    void halt();


    /**
     * It is the single consumer channel that emit event of the worker.
     * <p>
     * It can be {@code null} if not emission is supported.
     */
    default OneToOneWorkerChannel workerChannel() {
        return null;
    }


    /**
     * It is able to take a snapshot of the current recorder latencies.
     *
     * @param intervalHistogram the new histogram to be used to record latencies from now on
     * @return the old latencies histogram or {@code null} if none has been recorded.
     */
    default Histogram takeLatenciesSnapshot(Histogram intervalHistogram) {
        return null;
    }


    /**
     * When multiple workers are involved, set the number of the worker
     *
     * @param number the number of this worker
     */
    void setWorkerNumber(int number);
}
