package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a container class for multiple workerRuntimeInfos. It is responsible for
 * creating, starting and stopping multiple workerRuntimeInfos at once.
 */
public final class WorkerContainer {
    private static WorkerContainer instance;
    private WorkerOptions workerOptions;
    private List<WorkerRuntimeInfo> workerRuntimeInfos = new ArrayList<>();

    private static class WorkerRuntimeInfo {
        public Thread thread;
        public MaestroWorker worker;
    }

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
     * Start the execution of the workerRuntimeInfos for a predefined class
     *
     * @param clazz   The class associated with the workerRuntimeInfos
     * @param workers The workerRuntimeInfos to be monitored to provide performance counters data
     * @throws IllegalAccessException if unable to access the worker constructor
     * @throws InstantiationException if unable to instantiate the worker
     */
    public void start(final Class<MaestroWorker> clazz, Collection<? super MaestroWorker> workers)
            throws IllegalAccessException, InstantiationException {
        final int parallelCount = Integer.parseInt(workerOptions.getParallelCount());
        this.workerRuntimeInfos.clear();
        try {
            createAndStartWorkers(clazz, workerOptions, parallelCount, this.workerRuntimeInfos);
        } catch (Throwable t) {
            //interrupt any workers
            this.workerRuntimeInfos.forEach(info -> info.thread.interrupt());
            //cleanup
            this.workerRuntimeInfos.clear();
            throw t;
        }
        //the workers are started
        workers.addAll(this.workerRuntimeInfos.stream().map(info -> info.worker).collect(Collectors.toList()));
    }

    private static void createAndStartWorkers(final Class<MaestroWorker> clazz, WorkerOptions workerOptions, int workers, Collection<WorkerRuntimeInfo> workerRuntimeInfos) throws IllegalAccessException, InstantiationException {
        for (int i = 0; i < workers; i++) {
            final WorkerRuntimeInfo ri = new WorkerRuntimeInfo();
            ri.worker = clazz.newInstance();
            ri.worker.setWorkerOptions(workerOptions);
            ri.thread = new Thread(ri.worker);
            ri.thread.start();
            workerRuntimeInfos.add(ri);
        }
    }

    public void stop() {
        for (WorkerRuntimeInfo ri : workerRuntimeInfos) {
            ri.worker.stop();
        }
    }
}
