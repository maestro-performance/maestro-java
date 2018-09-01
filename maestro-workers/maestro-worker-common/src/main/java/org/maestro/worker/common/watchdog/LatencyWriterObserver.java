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

import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.worker.common.WorkerLatencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An observer for the watchdog that handles the latency writing
 * process based on the start/stop of the tests
 */
public class LatencyWriterObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(LatencyWriterObserver.class);
    private final WorkerLatencyWriter latencyWriter;
    private Thread latencyWriterThread;
    private final Thread shutdownThread;

    public LatencyWriterObserver(final WorkerLatencyWriter latencyWriter) {
        this.latencyWriter = latencyWriter;
        shutdownThread = new Thread(this::shutdown);

        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private void shutdown() {
        if (latencyWriterThread != null) {
            this.latencyWriterThread.interrupt();

            try {
                this.latencyWriterThread.join();
            } catch (InterruptedException e) {
                logger.error("Latency writer thread was interrupted: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onStart() {
        try {
            this.latencyWriterThread = new Thread(latencyWriter);
            latencyWriterThread.start();
            return true;
        }
        catch (Throwable t) {
            throw new MaestroException("Unable to start the latency writer", t);
        }
    }

    @Override
    public boolean onStop(final List<MaestroWorker> workers) {
        this.latencyWriterThread.interrupt();

        try {
            this.latencyWriterThread.join();
        } catch (InterruptedException e) {
            logger.error("Latency writer thread was interrupted: {}", e.getMessage(), e);
        }
        finally {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }

        return true;
    }
}
