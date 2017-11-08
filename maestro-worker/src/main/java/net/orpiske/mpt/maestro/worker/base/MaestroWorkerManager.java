package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.notes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class MaestroWorkerManager extends AbstractMaestroPeer {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    public MaestroWorkerManager(final String url, final String clientName) throws MaestroConnectionException {
        super(url, clientName);
    }


    protected void messageArrived(MaestroNote note) {
        logger.debug("Some message arrived: {}", note.toString());

        if (note instanceof PingRequest) {
            maestroMessageArrived((PingRequest) note);
        }
        if (note instanceof StatsRequest) {
            maestroMessageArrived((StatsRequest) note);
        }

        if (note instanceof FlushRequest) {
            maestroMessageArrived((FlushRequest) note);
        }

        if (note instanceof Halt) {
            maestroMessageArrived((Halt) note);
        }
        if (note instanceof SetRequest) {
            maestroMessageArrived((SetRequest) note);
        }

        if (note instanceof StartInspector) {
            maestroMessageArrived((StartInspector) note);
        }

        if (note instanceof StartReceiver) {
            maestroMessageArrived((StartReceiver) note);
        }
        if (note instanceof StartSender) {
            maestroMessageArrived((StartSender) note);
        }

        if (note instanceof StopInspector) {
            maestroMessageArrived((StopInspector) note);
        }

        if (note instanceof StopReceiver) {
            maestroMessageArrived((StopReceiver) note);
        }
        if (note instanceof StopSender) {
            maestroMessageArrived((StopSender) note);
        }

        if (note instanceof TestFailedNotification) {
            maestroMessageArrived((TestFailedNotification) note);
        }

        if (note instanceof TestSuccessfulNotification) {
            maestroMessageArrived((TestSuccessfulNotification) note);
        }

        if (note instanceof AbnormalDisconnect) {
            maestroMessageArrived((AbnormalDisconnect) note);
        }
    }


    protected void maestroMessageArrived(StatsRequest note) {
        logger.debug("Stats request received");
    }

    protected void maestroMessageArrived(FlushRequest note) {
        logger.debug("Flush request received");
    }

    protected void maestroMessageArrived(Halt note) {
        logger.debug("Halt request received");
    }

    protected void maestroMessageArrived(SetRequest note) {
        logger.debug("Set request received");
    }

    protected void maestroMessageArrived(StartInspector note) {
        logger.debug("Start inspector request received");
    }

    protected void maestroMessageArrived(StartReceiver note) {
        logger.debug("Stats request received");
    }

    protected void maestroMessageArrived(StartSender note) {
        logger.debug("Start sender request received");
    }

    protected void maestroMessageArrived(StopInspector note) {
        logger.debug("Stop inspector request received");
    }

    protected void maestroMessageArrived(StopReceiver note) {
        logger.debug("Stop receiver request received");
    }

    protected void maestroMessageArrived(StopSender note) {
        logger.debug("Stop sender request received");
    }

    protected void maestroMessageArrived(TestFailedNotification note) {
        logger.debug("Test failed notification received");
    }

    protected void maestroMessageArrived(TestSuccessfulNotification note) {
        logger.debug("Test successful notification received");
    }

    protected void maestroMessageArrived(AbnormalDisconnect note) {
        logger.debug("Abnormal disconnect notification received");
    }

    protected void maestroMessageArrived(PingRequest note) {
        logger.debug("Creation seconds.micro: {}.{}", note.getSec(), note.getUsec());

        Instant creation = Instant.ofEpochSecond(note.getSec(), note.getUsec() * 1000);
        Instant now = Instant.now();

        Duration d = Duration.between(creation, now);

        logger.debug("Elapsed: {}", d.getNano() / 1000);
    }
}
