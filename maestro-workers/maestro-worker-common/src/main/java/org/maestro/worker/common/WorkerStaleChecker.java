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

package org.maestro.worker.common;

import org.maestro.common.NonProgressingStaleChecker;
import org.maestro.common.StaleChecker;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Checks a group of workers to ensure they are not stale
 */
public class WorkerStaleChecker {
    private final List<? extends MaestroWorker> workers;
    private static final StaleChecker staleChecker = new NonProgressingStaleChecker(30);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /**
     * Constructor
     * @param workers group of workers to check
     */
    public WorkerStaleChecker(List<? extends MaestroWorker> workers) {
        this.workers = workers;
    }


    /**
     * Starts checking for staled workers
     */
    public void start() {
        executorService.scheduleAtFixedRate(this::staleCheck, 5, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops checking for staled workers
     */
    public void stop() {
        executorService.shutdown();
    }

    private void staleCheck() {
        long count = 0;

        for (MaestroWorker worker : workers) {
            if (worker.isRunning()) {
                count += worker.messageCount();
            }
        }

        if (staleChecker.isStale(count)) {
            workers.get(0).fail(new MaestroException("Worker has staled after exchanging %d messages", count));
        }
    }
}
