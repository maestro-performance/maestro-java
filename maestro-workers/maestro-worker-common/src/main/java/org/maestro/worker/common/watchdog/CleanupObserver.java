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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Cleans up the list of workers and do any other cleanup required
 */
public class CleanupObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(CleanupObserver.class);

    @Override
    public boolean onStop(List<MaestroWorker> workers) {
        logger.info("Cleaning up the list of workers");

        workers.clear();
        return false;
    }
}
