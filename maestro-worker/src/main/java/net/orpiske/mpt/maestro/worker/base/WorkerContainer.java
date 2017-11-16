package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.common.worker.WorkerSnapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class WorkerContainer {
    private static WorkerContainer instance;
    private WorkerOptions workerOptions;
    private List<MaestroWorker> workers = new LinkedList<>();

    private WorkerContainer() {}

    public synchronized static final WorkerContainer getInstance() {
        if (instance == null) {
            instance = new WorkerContainer();
        }

        return instance;
    }


    public WorkerOptions getWorkerOptions() {
        return workerOptions;
    }

    public void setWorkerOptions(WorkerOptions workerOptions) {
        this.workerOptions = workerOptions;
    }

    public void start(final Class<MaestroWorker> clazz, BlockingQueue<WorkerSnapshot> snapshots) throws IllegalAccessException, InstantiationException {
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
