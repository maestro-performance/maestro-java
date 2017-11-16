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

package net.orpiske.mpt.common.worker;

import java.util.concurrent.BlockingQueue;

/**
 * A common interface for any type of Maestro worker.
 */
public interface MaestroWorker {

    /**
     * Checks whether the worker is in running state.
     * @return true if it is in running state or false otherwise
     */
    boolean isRunning();

    /**
     * Sets the options for this worker
     * @param workerOptions
     */
    void setWorkerOptions(WorkerOptions workerOptions);

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
     * Provides the updated performance snapshot
     * @return
     */
    WorkerSnapshot stats();


    /**
     * Sets a queue for IPC-like communication of the performance
     * snapshot
     * @param queue the blocking queue to use as IPC-like mechanism
     */
    void setQueue(BlockingQueue<WorkerSnapshot> queue);

}
