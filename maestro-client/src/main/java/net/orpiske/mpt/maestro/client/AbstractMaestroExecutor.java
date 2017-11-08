package net.orpiske.mpt.maestro.client;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides an abstract interface that can be used to receive data from a Maestro broker.
 */
public class AbstractMaestroExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroCollectorExecutor.class);

    private AbstractMaestroPeer maestroPeer;
    private volatile boolean exit = false;

    /**
     * Constructor
     * @param maestroPeer a Maestro peer object is capable of exchange maestro data.
     * @throws MaestroConnectionException if unable to connect or subscribe
     */
    public AbstractMaestroExecutor(final AbstractMaestroPeer maestroPeer) throws MaestroConnectionException {
        this.maestroPeer = maestroPeer;

        logger.debug("Connecting the maestro broker");
        maestroPeer.connect();
    }


    /**
     * Get the Maestro peer
     * @return the maestro peer object
     */
    protected AbstractMaestroPeer getMaestroPeer() {
        return maestroPeer;
    }

    /**
     * Runs the executor
     */
    public final void run() {
        while (!exit) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Stops the executor
     */
    public final void stop() {
        try {
            maestroPeer.disconnect();
        } catch (MaestroConnectionException e) {
            logger.debug(e.getMessage(), e);
        }

        exit = true;
    }

}
