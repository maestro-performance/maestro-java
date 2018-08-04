package org.maestro.worker.common;

import org.maestro.common.evaluators.Evaluator;
import org.maestro.common.evaluators.LatencyEvaluator;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorkerEvaluatorChecker {
    private static final Logger logger = LoggerFactory.getLogger(WorkerEvaluatorChecker.class);
    private final List<? extends MaestroWorker> workers;
    private final Evaluator<?> evaluator;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /**
     * Constructor
     * @param workers group of workers to check
     */
    public WorkerEvaluatorChecker(List<? extends MaestroWorker> workers, final Evaluator<?> evaluator) {
        this.workers = workers;
        this.evaluator = evaluator;
    }


    /**
     * Starts checking for staled workers
     */
    public void start() {
        executorService.scheduleAtFixedRate(this::evalCheck, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops checking for staled workers
     */
    public void stop() {
        executorService.shutdown();
    }

    private void evalCheck() {
        if (!workers.get(0).isRunning()) {
            return;
        }

        if (!evaluator.eval()) {
            if (evaluator instanceof LatencyEvaluator) {
                logger.error("Exceed maximum acceptable latency. Failing the test ...");
                workers.get(0).fail(new MaestroException("The evaluation of the condition failed: the latency exceeds " +
                        "the maximum acceptable value"));

            }
            else {
                workers.get(0).fail(new MaestroException("The evaluation of the condition failed"));
            }
        }
    }
}
