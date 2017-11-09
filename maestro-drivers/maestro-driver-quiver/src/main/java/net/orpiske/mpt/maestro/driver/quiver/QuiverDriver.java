package net.orpiske.mpt.maestro.driver.quiver;

import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.Stats;
import net.ssorj.quiver.QuiverArrowJms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuiverDriver implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(QuiverDriver.class);

    private String brokerUrl;
    private String duration;
    private boolean verbose = false;
    private String messageSize;

    /*
    usage: quiver [-h] [-m COUNT] [--impl NAME] [--body-size COUNT] [--credit COUNT]
              [--timeout SECONDS] [--output DIRECTORY] [--init-only] [--quiet]
              [--verbose] ADDRESS
     */

    @Override
    public void setBroker(String url) {
        url.replace("amqp:", "");
    }

    @Override
    public void setDuration(String duration) {
        if (duration.matches("[a-zA-Z]")) {
            // TODO: decide what to do here.
        }
        else {
            this.duration = duration;
        }
    }

    @Override
    public void setLogLevel(String logLevel) {
        if (logLevel.equals("debug") || logLevel.equals("DEBUG")) {
            this.verbose = true;
        }
    }

    @Override
    public void setParallelCount(String parallelCount) {
        // TODO: unsupported ... what to do?
        logger.warn("Concurrent connections are not supported on this worker");
    }

    @Override
    public void setMessageSize(String messageSize) {
        if (messageSize.contains("~")) {
            logger.warn("Variable message sizes are not supported on this worker");
            this.messageSize = messageSize.replace("~", "");
        }
        else {
            this.messageSize = messageSize;
        }
    }

    @Override
    public void setThrottle(String value) {
        logger.warn("Concurrent connections are not supported on this worker");
    }

    @Override
    public void setRate(String rate) {
        logger.warn("Target rate is not supported on this worker");
    }

    @Override
    public void setFCL(String fcl) {
        logger.warn("Fail-condition-on-latency is not supported on this worker");
    }

    @Override
    public void start() {
        logger.info("Starting the worker");
    }

    @Override
    public void stop() {

    }

    @Override
    public void halt() {

    }

    @Override
    public Stats stats() {
        return null;
    }

    public int run() {
        String[] args = {""};

        try {
            QuiverArrowJms.doMain(args);
        } catch (Exception e) {
            e.printStackTrace();

            return 1;
        }

        return 0;
    }


}
