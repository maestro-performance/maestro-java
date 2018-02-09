package net.orpiske.mpt.test;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import net.orpiske.mpt.maestro.notes.PingResponse;
import net.orpiske.mpt.reports.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * A simple test executor that should be extensible for most usages
 */
public abstract class AbstractTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestExecutor.class);

    private final Maestro maestro;
    private final ReportsDownloader reportsDownloader;


    public AbstractTestExecutor(final Maestro maestro, final ReportsDownloader reportsDownloader) {
        this.maestro = maestro;
        this.reportsDownloader = reportsDownloader;

        logger.trace("Creating an abstract test executor");
    }

    protected Maestro getMaestro() {
        return maestro;
    }

    protected ReportsDownloader getReportsDownloader() {
        return reportsDownloader;
    }

    /**
     * Start connected peers
     * @throws MaestroConnectionException
     */
    protected void startServices() throws MaestroConnectionException {
        maestro.startReceiver();
        maestro.startInspector();
        maestro.startSender();
    }

    /**
     * Try to guess the number of connected peers
     * @return the number of connected peers (best guess)
     * @throws MaestroConnectionException
     * @throws InterruptedException
     */
    protected int getNumPeers() throws MaestroConnectionException, InterruptedException {
        int numPeers = 0;

        logger.debug("Collecting responses to ensure topic is clean prior to pinging nodes");
        maestro.collect();

        logger.debug("Sending ping request");
        maestro.pingRequest();

        Thread.sleep(5000);

        List<MaestroNote> replies = maestro.collect();
        for (MaestroNote note : replies) {
            if (note instanceof PingResponse) {
                numPeers++;
            }
        }

        return numPeers;
    }

    protected void processReplies(AbstractTestProcessor testProcessor, long repeat, int numPeers) {
        while (testProcessor.getNotifications() != numPeers) {
            List<MaestroNote> replies = getMaestro().collect(1000, 1);

            testProcessor.process(replies);
            repeat--;
            logger.debug("Estimated time for test completion: {} secs", repeat);

            if (repeat == 0) {
                break;
            }
        }
    }

}
