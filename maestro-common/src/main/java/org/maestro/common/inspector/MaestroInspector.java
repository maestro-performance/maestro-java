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

package org.maestro.common.inspector;

import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.test.MaestroTestProperties;
import org.maestro.common.worker.WorkerOptions;

import java.io.File;

public interface MaestroInspector extends TestDuration.TestProgress {

    void setUrl(String url);

    void setUser(String user);

    void setPassword(String password);

    void setWorkerOptions(WorkerOptions workerOptions) throws DurationParseException;

    default void setCommonProperties(MaestroTestProperties testProperties, WorkerOptions workerOptions) {
        testProperties.setParallelCount(workerOptions.getParallelCount());

        // Note: it already sets the variable size flag for variable message sizes
        testProperties.setMessageSize(workerOptions.getMessageSize());

        testProperties.setRate(workerOptions.getRate());
    }

    void setBaseLogDir(File logDir);

    void setEndpoint(MaestroReceiver endpoint);

    /**
     * Inspectors normally don't know how many messages have been
     * exchanged, therefore they should return 0 and handle the
     * success/failure notifications when the test is count-based
     * @return the message count
     */
    @Override
    default long messageCount() {
        return 0;
    }

    int start() throws Exception;

    void stop();
}
