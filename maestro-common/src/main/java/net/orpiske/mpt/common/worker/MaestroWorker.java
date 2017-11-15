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

public interface MaestroWorker {

    /**
     * Set the broker url
     * @param url
     */
    void setBroker(final String url);

    /**
     * Set the test duration
     * @param duration
     */
    void setDuration(final String duration);

    /**
     * Set the log level
     * @param logLevel
     */
    void setLogLevel(final String logLevel);

    /**
     * Sets the number of concurrent connections
     * @param parallelCount
     */
    void setParallelCount(final String parallelCount);

    /**
     * Sets the message size
     * @param messageSize
     */
    void setMessageSize(final String messageSize);

    /**
     * Sets the throttling value
     * @param value
     */
    void setThrottle(final String value);

    /**
     * Sets the target rate
     * @param rate
     */
    void setRate(final String rate);

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
     * @param queue
     */
    void setQueue(BlockingQueue<WorkerSnapshot> queue);

}
