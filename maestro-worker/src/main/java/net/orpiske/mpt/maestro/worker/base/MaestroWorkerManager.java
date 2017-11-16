package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.common.worker.*;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.client.MaestroClient;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.notes.*;
import net.orpiske.mpt.maestro.notes.InternalError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MaestroWorkerManager extends AbstractMaestroPeer {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private MaestroClient client;
    private WorkerContainer container = WorkerContainer.getInstance();
    private String host;
    private Class<MaestroWorker> workerClass;

    private File logDir;

    private BlockingQueue<WorkerSnapshot> queue;
    private WorkerOptions workerOptions;

    private boolean running = true;


    public MaestroWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                final Class<MaestroWorker> workerClass) throws MaestroException
    {
        super(maestroURL, role);

        client = new MaestroClient(maestroURL);
        client.connect();

        this.workerClass = workerClass;
        this.host = host;
        this.logDir = logDir;

        queue = new LinkedBlockingQueue<>();
        workerOptions = new WorkerOptions();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private File findTestLogDir() {
        File testLogDir = new File(logDir, "0");
        int count = 0;

        while (testLogDir.exists()) {
            testLogDir = new File(logDir, Integer.toString(count));
            count++;
        }

        testLogDir.mkdirs();
        return testLogDir;
    }

    private void replyOk() {
        logger.trace("Sending the OK reponse from {}", this.toString());
        OkResponse okResponse = new OkResponse();


        okResponse.setName(clientName + "@" + host);
        okResponse.setId(getId());

        try {
            client.publish(MaestroTopics.MAESTRO_TOPIC, okResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    private void replyInternalError() {
        logger.trace("Sending the internal error reponse from {}", this.toString());
        InternalError errResponse = new InternalError();


        errResponse.setName(clientName + "@" + host);
        errResponse.setId(getId());

        try {
            client.publish(MaestroTopics.MAESTRO_TOPIC, errResponse);
        } catch (Exception e) {
            logger.error("Unable to publish the OK response {}", e.getMessage(), e);
        }
    }

    protected void noteArrived(MaestroNote note) throws IOException, MaestroConnectionException {
        logger.debug("Some message arrived: {}", note.toString());

        if (note instanceof PingRequest) {
            noteArrived((PingRequest) note);
        }
        if (note instanceof StatsRequest) {
            noteArrived((StatsRequest) note);
        }

        if (note instanceof FlushRequest) {
            noteArrived((FlushRequest) note);
        }

        if (note instanceof Halt) {
            noteArrived((Halt) note);
        }
        if (note instanceof SetRequest) {
            noteArrived((SetRequest) note);
        }

        if (note instanceof StartInspector) {
            noteArrived((StartInspector) note);
        }

        if (note instanceof StartReceiver) {
            noteArrived((StartReceiver) note);
        }
        if (note instanceof StartSender) {
            noteArrived((StartSender) note);
        }

        if (note instanceof StopInspector) {
            noteArrived((StopInspector) note);
        }

        if (note instanceof StopReceiver) {
            noteArrived((StopReceiver) note);
        }
        if (note instanceof StopSender) {
            noteArrived((StopSender) note);
        }

        if (note instanceof TestFailedNotification) {
            noteArrived((TestFailedNotification) note);
        }

        if (note instanceof TestSuccessfulNotification) {
            noteArrived((TestSuccessfulNotification) note);
        }

        if (note instanceof AbnormalDisconnect) {
            noteArrived((AbnormalDisconnect) note);
        }
    }


    protected void noteArrived(StatsRequest note) {
        logger.debug("Stats request received");
    }

    protected void noteArrived(FlushRequest note) {
        logger.debug("Flush request received");
    }

    protected void noteArrived(Halt note) {
        logger.debug("Halt request received");

        running = false;
    }

    protected void noteArrived(SetRequest note) {
        logger.debug("Set request received");

        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_SET_BROKER: {
                workerOptions.setBrokerURL(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_DURATION_TYPE: {
                workerOptions.setDuration(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_LOG_LEVEL: {
                workerOptions.setLogLevel(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT: {
                workerOptions.setParallelCount(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE: {
                workerOptions.setMessageSize(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_THROTTLE: {
                workerOptions.setThrottle(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_RATE: {
                workerOptions.setRate(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_FCL: {
                workerOptions.setFcl(note.getValue());
            }
        }

        container.setWorkerOptions(workerOptions);
        replyOk();
    }

    private void doWorkerStart() {
        try {
            container.start(workerClass, queue);
            replyOk();
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            replyInternalError();
        }
    }

    protected void noteArrived(StartInspector note) {
        logger.debug("Start inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }
    }

    protected void noteArrived(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }

//        if (worker instanceof MaestroReceiverWorker) {
//            MaestroReceiverWorker mrw = (MaestroReceiverWorker) worker;
//
//            try {
//                File testLogDir = findTestLogDir();
//
//                mrw.setRateWriter(new RateWriter(new File(testLogDir,"receiver-rate.gz")));
//                mrw.setLatencyWriter(new LatencyWriter(new File(testLogDir,"receiver-latency.hdr")));
//                mrw.setQueue(queue);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            if (mrw instanceof Runnable) {
//                logger.debug("Thread-capable receiver detected. Launching threads");
//                Thread t1 = new Thread((Runnable) mrw,"receiver-worker-1");
//
//                t1.start();
//            }
//            else {
//                logger.debug("Single-thread sender detected.");
//                worker.start();
//            }
//        }
//        replyOk();
    }

    protected void noteArrived(StartSender note) {
        logger.debug("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }


//        if (worker instanceof MaestroSenderWorker) {
//            MaestroSenderWorker msw = (MaestroSenderWorker) worker;
//
//            try {
//                File testLogDir = findTestLogDir();
//
//                msw.setRateWriter(new RateWriter(new File(testLogDir, "sender-rate.gz")));
//                msw.setQueue(queue);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            if (msw instanceof Runnable) {
//                logger.debug("Thread-capable sender detected. Launching threads");
//                Thread t1 = new Thread((Runnable) msw,"sender-worker-1");
//
//                t1.start();
//            }
//            else {
//                logger.debug("Single-thread sender detected.");
//                msw.start();
//            }
//        }
//        replyOk();
    }

    protected void noteArrived(StopInspector note) {
        logger.debug("Stop inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        replyOk();
    }

    protected void noteArrived(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        replyOk();
    }

    protected void noteArrived(StopSender note) {
        logger.debug("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        replyOk();
    }

    protected void noteArrived(TestFailedNotification note) {
        logger.info("Test failed notification received from {}: {}", note.getName(), note.getMessage());
        container.stop();
    }

    protected void noteArrived(TestSuccessfulNotification note) {
        logger.info("Test successful notification received from {}: {}", note.getName(), note.getMessage());
    }

    protected void noteArrived(AbnormalDisconnect note) {
        logger.info("Abnormal disconnect notification received from {}: {}", note.getName(), note.getMessage());
    }

    protected void noteArrived(PingRequest note) throws IOException, MaestroConnectionException {
        logger.debug("Creation seconds.micro: {}.{}", note.getSec(), note.getUsec());

        Instant creation = Instant.ofEpochSecond(note.getSec(), note.getUsec() * 1000);
        Instant now = Instant.now();

        Duration d = Duration.between(creation, now);

        logger.debug("Elapsed: {}", d.getNano() / 1000);
        PingResponse response = new PingResponse();

        response.setElapsed(d.getNano() / 1000);
        response.setName(clientName + "@" + host);
        response.setId(getId());

        client.publish(MaestroTopics.MAESTRO_TOPIC, response);
    }
}
