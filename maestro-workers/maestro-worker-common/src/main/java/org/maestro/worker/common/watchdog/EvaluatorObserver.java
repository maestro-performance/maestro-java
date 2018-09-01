package org.maestro.worker.common.watchdog;

import org.maestro.common.worker.MaestroWorker;
import org.maestro.worker.common.WorkerEvaluatorChecker;

import java.util.List;

public class EvaluatorObserver implements WatchdogObserver {
    private final WorkerEvaluatorChecker evaluatorChecker;

    public EvaluatorObserver(WorkerEvaluatorChecker evaluatorChecker) {
        this.evaluatorChecker = evaluatorChecker;
    }

    @Override
    public boolean onStart() {
        evaluatorChecker.start();
        return true;
    }

    @Override
    public boolean onStop(List<MaestroWorker> workers) {
        evaluatorChecker.stop();
        return true;
    }
}
