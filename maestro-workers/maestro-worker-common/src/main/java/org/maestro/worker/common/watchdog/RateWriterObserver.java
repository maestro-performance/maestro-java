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
import org.maestro.worker.common.WorkerRateWriter;
import org.maestro.worker.common.WorkerRuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An observer for the watchdog that handles the rate writing
 * process based on the start/stop of the tests
 */
public class RateWriterObserver implements WatchdogObserver {
    private static final Logger logger = LoggerFactory.getLogger(RateWriterObserver.class);
    private final WorkerRateWriter workerRateWriter;
    private Thread rateWriterThread;
    private Thread shutdownThread;

    public RateWriterObserver(final WorkerRateWriter workerRateWriter) {
        this.workerRateWriter = workerRateWriter;

        shutdownThread = new Thread(this::shutdown);
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private void shutdown() {
        if (this.rateWriterThread != null) {
            try {
                this.rateWriterThread.join();
            } catch (InterruptedException e) {
                logger.error("Rate writer thread was interrupted: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onStart() {
        try {
            rateWriterThread = new Thread(workerRateWriter);
            rateWriterThread.start();
        }
        catch (Throwable t) {
            throw new MaestroException("Unable to start rate writer", t);
        }

        return true;
    }

    @Override
    public boolean onStop(final List<WorkerRuntimeInfo> workerRuntimeInfos) {
        this.rateWriterThread.interrupt();

        try {
            this.rateWriterThread.join();
        } catch (InterruptedException e) {
            logger.error("Rate writer thread was interrupted: {}", e.getMessage(), e);
        }
        finally {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }

        return true;
    }
}
