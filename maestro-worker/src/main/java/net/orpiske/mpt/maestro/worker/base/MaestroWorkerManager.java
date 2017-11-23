package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.common.test.TestProperties;
import net.orpiske.mpt.common.worker.*;
import net.orpiske.mpt.maestro.MaestroReceiverClient;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.client.MaestroDeserializer;
import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import net.orpiske.mpt.maestro.notes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaestroWorkerManager extends AbstractMaestroPeer<MaestroEvent> implements MaestroEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private MaestroReceiverClient client;
    private WorkerContainer container;
    private String host;
    private Class<MaestroWorker> workerClass;

    private File logDir;

    private WorkerOptions workerOptions;
    private Thread writerThread;

    private boolean running = true;

    public MaestroWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                final Class<MaestroWorker> workerClass) throws MaestroException {
        super(maestroURL, role, MaestroDeserializer::deserializeEvent);

        client = new MaestroReceiverClient(maestroURL, clientName, host, id);

        this.workerClass = workerClass;
        this.host = host;
        this.logDir = logDir;
        this.container = WorkerContainer.getInstance(client);

        workerOptions = new WorkerOptions();
    }

    @Override
    public void connect() throws MaestroConnectionException {
        super.connect();

        client.connect();
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

    @Override
    protected final void noteArrived(MaestroEvent note) throws IOException, MaestroConnectionException {
        logger.debug("Some message arrived: {}", note.toString());
        note.notify(this);
    }


    @Override
    public void handle(StatsRequest note) {
        logger.debug("Stats request received");
    }

    @Override
    public void handle(FlushRequest note) {
        logger.debug("Flush request received");
    }

    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        running = false;
    }

    @Override
    public void handle(SetRequest note) {
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
        client.replyOk();
    }

    private void writeTestProperties(File testLogDir) throws IOException {
        TestProperties testProperties = new TestProperties();

        testProperties.setBrokerUri(workerOptions.getBrokerURL());
        try {
            testProperties.setDuration(workerOptions.getDuration());
        } catch (DurationParseException e) {
            logger.warn("Failed to parse duration while saving the test properties: {}", e.getMessage(), e);
        }
        testProperties.setParallelCount(workerOptions.getParallelCount());

        // Note: it already sets the variable size flag for variable message sizes
        testProperties.setMessageSize(workerOptions.getMessageSize());

        testProperties.setRate(workerOptions.getRate());
        testProperties.setFcl(workerOptions.getFcl());

        // TODO: collect this
        testProperties.setApiName(workerClass.getName());
        testProperties.setApiVersion("undefined");

        testProperties.write(new File(testLogDir, "test.properties"));
    }

    private void doWorkerStart() {
        File testLogDir = findTestLogDir();

        try {
            writeTestProperties(testLogDir);
        } catch (IOException e) {
            logger.warn("Unable to write test properties: {}", e.getMessage(), e);
        }

        try {
            final List<MaestroWorker> workers = new ArrayList<>();
            logger.debug("Starting the worker {}", workerClass);
            container.start(workerClass, workers);

            logger.debug("Creating the writer thread");
            WorkerDataWriter wdw = new WorkerDataWriter(workers);

            writerThread = new Thread(wdw);

            logger.debug("Starting the writer thread");
            writerThread.start();

            client.replyOk();
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            client.replyInternalError();
        }
    }



    @Override
    public void handle(StartInspector note) {
        logger.debug("Start inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }
    }

    @Override
    public void handle(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.debug("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            doWorkerStart();
        }
    }

    @Override
    public void handle(StopInspector note) {
        logger.debug("Stop inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(StopSender note) {
        logger.debug("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(TestFailedNotification note) {
        logger.info("Test failed notification received from {}: {}", note.getName(), note.getMessage());
        container.stop();
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        logger.info("Test successful notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        logger.info("Abnormal disconnect notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        client.pingResponse(note.getSec(), note.getUsec());
    }
}
