package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.WorkerSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class WorkerDataWriter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WorkerDataWriter.class);

    private BlockingQueue<WorkerSnapshot> queue;

    public WorkerDataWriter(BlockingQueue<WorkerSnapshot> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        logger.debug("Running the data writer");

        while (true) {
            for (int i = 0; i < queue.size(); i++) {
                WorkerSnapshot workerSnapshot = null;
                try {
                    workerSnapshot = queue.take();

                    logger.debug("Snapshot: {}", workerSnapshot);
                } catch (InterruptedException e) {
                    logger.debug("Writer thread was interrupted while fetching last snapshot: {}", e.getMessage(), e);
                    break;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.debug("Writer thread was interrupted: {}", e.getMessage(), e);
            }
        }
    }
}
