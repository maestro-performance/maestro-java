package org.maestro.worker.base;

import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.*;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.maestro.worker.base.WorkerStateInfoUtil.isCleanExit;

/**
 * A specialized worker manager that can manage concurrent test workers (ie.: senders/receivers)
 */
public class TestWorkerManager extends MaestroWorkerManager {
    private static final Logger logger = LoggerFactory.getLogger(TestWorkerManager.class);
    private static final long TIMEOUT_STOP_WORKER_MILLIS = 1_000;

    private final WorkerContainer container;
    private final Class<MaestroWorker> workerClass;
    private final File logDir;
    private Thread latencyWriterThread;
    private Thread rateWriterThread;

    /**
     * Constructor
     * @param maestroURL
     * @param role
     * @param host
     * @param logDir
     * @param workerClass
     * @throws MaestroException
     */
    public TestWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                             final Class<MaestroWorker> workerClass, final MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);

        this.container = WorkerContainer.getInstance(getClient());
        this.workerClass = workerClass;
        this.logDir = logDir;
    }

    private boolean doWorkerStart() {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start a new test, but a test execution is already in progress");
            getClient().notifyFailure("Test already in progress");

            return false;
        }

        final File testLogDir = WorkerLogUtils.findTestLogDir(logDir);

        try {
            super.writeTestProperties(testLogDir);

            final List<MaestroWorker> workers = new ArrayList<>();

            logger.debug("Starting the workers {}", workerClass);
            container.start(workerClass, workers, this::onStoppedWorkers);

            if (workers.isEmpty()) {
                logger.warn("No workers has been started!");
            } else {
                logger.debug("Creating the writer threads");
                WorkerLatencyWriter latencyWriter = new WorkerLatencyWriter(testLogDir, workers);
                this.latencyWriterThread = new Thread(latencyWriter);
                WorkerChannelWriter rateWriter = new WorkerChannelWriter(testLogDir, workers);
                this.rateWriterThread = new Thread(rateWriter);
                logger.debug("Starting the writers threads");

                this.latencyWriterThread.start();
                this.rateWriterThread.start();

                //TODO handle shutdown gently
                Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownAndWaitWriters));
            }

            getClient().replyOk();
            return true;
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }

        return false;
    }

    private void shutdownAndWaitWriters(){
        if (this.rateWriterThread != null) {
            this.rateWriterThread.interrupt();
        }
        if (this.latencyWriterThread != null) {
            this.latencyWriterThread.interrupt();
        }
        if (this.rateWriterThread != null) {
            try {
                this.rateWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.latencyWriterThread != null) {
            try {
                this.latencyWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean awaitWorkers(long startWaitingWorkersEpochMillis, final List<WorkerRuntimeInfo> workers) {
        final long deadLine = startWaitingWorkersEpochMillis + (workers.size() * TIMEOUT_STOP_WORKER_MILLIS * 2);
        //workers are being stopped, just need to check if they have finished their jobs
        boolean allFinished = false;
        while (!allFinished && System.currentTimeMillis() < deadLine) {
            allFinished = true;
            for (int i = 0, size = workers.size(); i < size; i++) {
                final WorkerRuntimeInfo workerRuntimeInfo = workers.get(i);
                try {
                    workerRuntimeInfo.thread.join(TIMEOUT_STOP_WORKER_MILLIS);
                } catch (InterruptedException e) {
                    //no op, just retry
                } finally {
                    allFinished = !workerRuntimeInfo.thread.isAlive();
                    if (!allFinished) {
                        break;
                    }
                }
            }
        }
        return allFinished;
    }

    /**
     * It should be called by a different thread/concurrently too (eg WatchDog) so it can't modify any this.* members or workers too.
     */
    private void onStoppedWorkers(List<WorkerRuntimeInfo> workers) {
        try {
            if (this.rateWriterThread != null || this.latencyWriterThread != null) {
                final long startWaitingWorkers = System.currentTimeMillis();
                if (!workers.isEmpty()) {
                    final boolean allFinished = awaitWorkers(startWaitingWorkers, workers);
                    if (!allFinished) {
                        logger.warn("The writer will be forced to stop with alive workers");
                    }
                }

                shutdownAndWaitWriters();
                final long elapsedMillis = System.currentTimeMillis() - startWaitingWorkers;
                logger.info("Awaiting workers and shutting down writers took {} ms", elapsedMillis);
            }

            boolean failed = false;
            for (WorkerRuntimeInfo ri : workers) {
                WorkerStateInfo wsi = ri.worker.getWorkerState();

                if (!isCleanExit(wsi)) {
                    failed = true;
                    break;
                }
            }

            WorkerLogUtils.createSymlinks(logDir, failed);
        } finally {
            //reset it for new incoming tests
            setWorkerOptions(new WorkerOptions());
        }
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);
        container.setWorkerOptions(getWorkerOptions());

        getClient().replyOk();
    }

    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        container.stop();
        setRunning(false);
    }

    @Override
    public void handle(StartInspector note) {
        logger.debug("Start inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.debug("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StopInspector note) {
        logger.debug("Stop inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }

    @Override
    public void handle(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }

    @Override
    public void handle(StopSender note) {
        logger.debug("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        getClient().replyOk();
    }
}
