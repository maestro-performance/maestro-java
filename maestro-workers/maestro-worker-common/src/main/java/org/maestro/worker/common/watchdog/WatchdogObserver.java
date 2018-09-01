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

package org.maestro.worker.common.watchdog;

import org.maestro.common.worker.MaestroWorker;

import java.util.List;

/**
 * Actions that are run when the watchdog change state. For now it handles only
 * start and stop state changes
 */
public interface WatchdogObserver {

    /**
     * Actions that run on watchdog start
     * @return true if it should continue or false otherwise
     */
    default boolean onStart() {
        return true;
    }

    /**
     * Actions that run on watchdog stop
     * @param workerRuntimeInfos a list of active workers and their information
     * @return true if processing should continue or false otherwise
     */
    default boolean onStop(final List<MaestroWorker> workerRuntimeInfos) {
        return true;
    }

}
