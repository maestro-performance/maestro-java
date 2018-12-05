/*
 * Copyright 2018 Otavio Rodolfo Piske
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
import org.maestro.worker.common.WorkerStaleChecker;

import java.util.List;

/**
 * An observer that checks if the workers have stopped sending data
 */
public class StaleObserver implements WatchdogObserver {
    private final WorkerStaleChecker workerStaleChecker;

    public StaleObserver(WorkerStaleChecker workerStaleChecker) {
        this.workerStaleChecker = workerStaleChecker;
    }

    @Override
    public boolean onStart() {
        workerStaleChecker.start();
        return true;
    }

    @Override
    public boolean onStop(List<MaestroWorker> workers) {
        workerStaleChecker.stop();
        return true;
    }
}
