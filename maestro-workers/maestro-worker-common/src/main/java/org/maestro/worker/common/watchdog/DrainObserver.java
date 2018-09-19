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

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.duration.DurationDrain;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.worker.common.WorkerContainer;
import org.maestro.worker.common.container.initializers.WorkerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DrainObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(DrainObserver.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final WorkerOptions workerOptions;
    private final WorkerContainer workerContainer = new WorkerContainer();
    private final WorkerInitializer workerInitializer;
    private final MaestroReceiverClient client;

    public DrainObserver(final WorkerOptions workerOptions, final WorkerInitializer workerInitializer,
                         final MaestroReceiverClient client) {
        this.workerOptions = new WorkerOptions(workerOptions);
        this.workerOptions.setDuration(DurationDrain.DURATION_DRAIN_FORMAT);

        this.workerInitializer = workerInitializer;
        this.client = client;
    }


    @Override
    public boolean onStop(List<MaestroWorker> workers) {
        long drainDeadline = config.getLong("worker.drain.deadline.secs", 45);

        int count = workerOptions.getParallelCountAsInt();
        try {
            workerContainer.create(workerInitializer, count);

            logger.info("Drain the queues for up to {} seconds after the test was executed", drainDeadline);
            workerContainer.start();

            workerContainer.waitForComplete(drainDeadline);
            logger.info("Drain completed successfully");

            client.notifyDrainComplete(true, "Drain completed successfully");
        } catch (Throwable t) {
            logger.error("Unable to start drain workers: {}", t.getMessage(), t);
            client.notifyDrainComplete(false, "Drain completed with warnings");
        }

        return true;
    }
}
