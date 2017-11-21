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

import net.orpiske.mpt.common.duration.TestDuration;
import org.HdrHistogram.Histogram;
import net.orpiske.mpt.common.client.MaestroReceiver;

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
     * @param workerOptions
     */
    void setWorkerOptions(WorkerOptions workerOptions);

    /**
     * Sets an endpoint that workers can use to send notifications
     * @param endpoint
     */
    void setMaestroEndpoint(MaestroReceiver endpoint);

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
     * It is able to take a snapshot of the current recorder latencies.
     *
     * @param intervalHistogram the new histogram to be used to record latencies from now on
     * @return the old latencies histogram or {@code null} if none has been recorded.
     */
    default Histogram takeLatenciesSnapshot(Histogram intervalHistogram) {
        return null;
    }


}
