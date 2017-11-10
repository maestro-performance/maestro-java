package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.common.worker.MaestroInspectorWorker;
import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.client.MaestroClient;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.notes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class MaestroWorkerManager extends AbstractMaestroPeer {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private MaestroClient client;
    private MaestroWorker worker;
    private String host;

    private boolean running = true;

    public MaestroWorkerManager(final String url, final String role, final String host, final MaestroWorker worker) throws MaestroException {
        super(url, role);

        client = new MaestroClient(url);
        client.connect();

        this.worker = worker;
        this.host = host;
    }

    @Override
    public boolean isRunning() {
        return running;
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
                worker.setBroker(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_DURATION_TYPE: {
                worker.setDuration(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_LOG_LEVEL: {
                worker.setLogLevel(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT: {
                worker.setParallelCount(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE: {
                worker.setMessageSize(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_THROTTLE: {
                worker.setThrottle(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_RATE: {
                worker.setRate(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_FCL: {
                worker.setFCL(note.getValue());
                break;
            }
        }
    }

    protected void noteArrived(StartInspector note) {
        logger.debug("Start inspector request received");

        if (worker instanceof MaestroInspectorWorker) {
            worker.start();
        }
    }

    protected void noteArrived(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (worker instanceof MaestroReceiverWorker) {
            worker.start();
        }
    }

    protected void noteArrived(StartSender note) {
        logger.debug("Start sender request received");

        if (worker instanceof MaestroSenderWorker) {
            worker.start();
        }
    }

    protected void noteArrived(StopInspector note) {
        logger.debug("Stop inspector request received");

        if (worker instanceof MaestroInspectorWorker) {
            worker.stop();
        }
    }

    protected void noteArrived(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (worker instanceof MaestroReceiverWorker) {
            worker.stop();
        }
    }

    protected void noteArrived(StopSender note) {
        logger.debug("Stop sender request received");

        if (worker instanceof MaestroSenderWorker) {
            worker.stop();
        }
    }

    protected void noteArrived(TestFailedNotification note) {
        logger.debug("Test failed notification received");
    }

    protected void noteArrived(TestSuccessfulNotification note) {
        logger.debug("Test successful notification received");
    }

    protected void noteArrived(AbnormalDisconnect note) {
        logger.debug("Abnormal disconnect notification received");
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
