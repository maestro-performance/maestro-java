package org.maestro.worker.common.watchdog;

import org.maestro.worker.common.WorkerEvaluatorChecker;
import org.maestro.worker.common.WorkerRuntimeInfo;

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
    public boolean onStop(List<WorkerRuntimeInfo> workerRuntimeInfos) {
        evaluatorChecker.stop();
        return true;
    }
}
