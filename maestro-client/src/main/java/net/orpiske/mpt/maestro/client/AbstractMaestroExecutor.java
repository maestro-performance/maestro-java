package net.orpiske.mpt.maestro.client;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides an abstract interface that can be used to receive data from a Maestro broker.
 */
public class AbstractMaestroExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollectorExecutor.class);

    private final AbstractMaestroPeer maestroPeer;

    /**
     * Constructor
     * @param maestroPeer a Maestro peer object is capable of exchange maestro data.
     * @throws MaestroConnectionException if unable to connect or subscribe
     */
    public AbstractMaestroExecutor(final AbstractMaestroPeer maestroPeer) throws MaestroConnectionException {
        this.maestroPeer = maestroPeer;
    }


    /**
     * Get the Maestro peer
     * @return the maestro peer object
     */
    protected AbstractMaestroPeer getMaestroPeer() {
        return maestroPeer;
    }

    /**
     * Start running the executor
     * @param topics the list of topics associated with this executor
     * @throws MaestroConnectionException if unable to connect to the broker and subscribe to the topics
     */
    public void start(final String[] topics) throws MaestroConnectionException {
        logger.debug("Connecting the maestro broker");

        maestroPeer.connect();
        maestroPeer.subscribe(topics);
    }

    /**
     * Runs the executor
     */
    public final void run() {
        while (maestroPeer.isRunning()) {
            try {
                logger.trace("Waiting for data ...");

                if (!maestroPeer.isConnected()) {
                    logger.error("Disconnected from the broker: reconnecting");
                    maestroPeer.connect();
                }

                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Stops the executor
     */
    public void stop() {
        try {
            logger.debug("Disconnecting the peer");
            maestroPeer.disconnect();
        } catch (MaestroConnectionException e) {
            logger.debug(e.getMessage(), e);
        }
    }

}
