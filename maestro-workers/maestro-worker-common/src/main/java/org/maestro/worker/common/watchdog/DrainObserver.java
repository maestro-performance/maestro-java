package org.maestro.worker.common.watchdog;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.MaestroReceiverClient;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.duration.DurationDrain;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.worker.common.WorkerContainer;
import org.maestro.worker.common.WorkerRuntimeInfo;
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
    public boolean onStop(List<WorkerRuntimeInfo> workerRuntimeInfos) {
        final int drainRetries = (config.getInt("worker.auto.drain.retries", 10) + 5);

        int count = workerOptions.getParallelCountAsInt();
        try {
            workerContainer.create(workerInitializer, count);

            logger.info("Starting to drain the queues after the test was executed");
            workerContainer.start(null);

            workerContainer.waitForComplete(drainRetries * 1000);
            logger.info("Drain completed successfully");
            client.notifyDrainComplete("Drain completed successfully");
        } catch (Throwable t) {
            logger.error("Unable to start drain workers: {}", t.getMessage(), t);
            client.notifyDrainComplete("Drain completed with warnings");
        }

        return true;
    }
}
