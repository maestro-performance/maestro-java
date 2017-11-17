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
        try {
            while (true) {
                //will block until an interrupt will arrie
                final WorkerSnapshot workerSnapshot = queue.take();
                logger.debug("Snapshot: {}", workerSnapshot);
            }
        } catch (InterruptedException e) {
            logger.debug("Writer thread was interrupted while fetching last snapshot: {}", e.getMessage(), e);
        }
        //drain the last remaining snapshots (if any)
        logger.debug("Writer thread is draining any remaining snapshots after being interrupted");
        long remainingSnapshots = 0;
        WorkerSnapshot workerSnapshot;
        while ((workerSnapshot = queue.poll()) != null) {
            remainingSnapshots++;
            logger.debug("Snapshot: {}", workerSnapshot);
        }
        logger.debug("Writer thread has drained {} remaining snapshots after being interrupted", remainingSnapshots);
    }
}
