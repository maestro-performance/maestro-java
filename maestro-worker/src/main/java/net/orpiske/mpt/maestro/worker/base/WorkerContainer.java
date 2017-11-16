package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.common.worker.WorkerSnapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * This is a container class for multiple workers. It is responsible for
 * creating, starting and stopping multiple workers at once.
 */
public final class WorkerContainer {
    private static WorkerContainer instance;
    private WorkerOptions workerOptions;
    private List<MaestroWorker> workers = new LinkedList<>();

    private WorkerContainer() {}

    /**
     * Gets and instance of the container
     * @return
     */
    public synchronized static final WorkerContainer getInstance() {
        if (instance == null) {
            instance = new WorkerContainer();
        }

        return instance;
    }


    /**
     * Sets the worker options for the instance.
     * @param workerOptions the worker options to set
     */
    public void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }


    /**
     * Start the execution of the workers for a predefined class
     * @param clazz The class associated with the workers
     * @param snapshots The performance snapshot queue containing the performance test data
     * @throws IllegalAccessException if unable to access the worker constructor
     * @throws InstantiationException if unable to instantiate the worker
     */
    public void start(final Class<MaestroWorker> clazz, BlockingQueue<WorkerSnapshot> snapshots)
            throws IllegalAccessException, InstantiationException
    {
        int parallelCount = Integer.parseInt(workerOptions.getParallelCount());

        for (int i = 0; i < parallelCount; i++) {
            MaestroWorker w = clazz.newInstance();

            w.setWorkerOptions(workerOptions);
            w.setQueue(snapshots);

            w.start();
            workers.add(w);
        }
    }


    public void stop() {
        for (MaestroWorker worker : workers) {
            worker.stop();
        }
    }
}
