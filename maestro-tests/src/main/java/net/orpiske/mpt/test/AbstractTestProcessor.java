package net.orpiske.mpt.test;

import net.orpiske.mpt.common.NodeUtils;
import net.orpiske.mpt.maestro.client.MaestroNoteProcessor;
import net.orpiske.mpt.maestro.notes.PingResponse;
import net.orpiske.mpt.maestro.notes.TestFailedNotification;
import net.orpiske.mpt.maestro.notes.TestSuccessfulNotification;
import net.orpiske.mpt.reports.ReportsDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a generic/reusable test processor that evaluates the individual
 * test responses for every peer on the test cluster and sets the test results
 * accordingly
 */
public abstract class AbstractTestProcessor extends MaestroNoteProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestProcessor.class);
    // Default wait time, in seconds, for the workers to flush their data
    public static final int DEFAULT_WAIT_TIME = 5;

    private final ReportsDownloader reportsDownloader;
    private final AbstractTestProfile testProfile;

    private boolean failed = false;
    private int notifications = 0;

    private int flushWaitSeconds = DEFAULT_WAIT_TIME;

    /**
     * Constructor
     * @param testProfile test profile in use
     * @param reportsDownloader A ReportsDownloader instance for downloading the logs and results
     *                          from each of the peers in the test cluster
     */
    public AbstractTestProcessor(AbstractTestProfile testProfile, ReportsDownloader reportsDownloader) {
        this.testProfile = testProfile;
        this.reportsDownloader = reportsDownloader;
    }

    @Override
    protected void processPingResponse(PingResponse note) {
        logger.debug("Elapsed time from {}: {} ms", note.getName(), note.getElapsed());
    }

    protected int getFlushWaitSeconds() {
        return flushWaitSeconds;
    }

    protected void setFlushWaitSeconds(int flushWaitSeconds) {
        this.flushWaitSeconds = flushWaitSeconds;
    }

    // Give some time for the backends to flush their data to disk
    // before downloading
    private void waitForFlush() {
        logger.info("Waiting for {} seconds for the backends to flush their data", flushWaitSeconds);
        try {
            Thread.sleep(flushWaitSeconds * 1000);
        } catch (InterruptedException e) {

        }
    }

    @Override
    protected void processNotifySuccess(TestSuccessfulNotification note) {
        logger.info("Test successful on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameters used: " + testProfile.toString());

        String type = NodeUtils.getTypeFromName(note.getName());
        String host = NodeUtils.getHostFromName(note.getName());

        waitForFlush();

        reportsDownloader.setReportResultTypeDir("success");
        reportsDownloader.downloadLastSuccessful(type, host, note.getName());

        notifications++;
    }

    @Override
    protected void processNotifyFail(TestFailedNotification note) {
        logger.info("Test failed on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameter used");

        String type = NodeUtils.getTypeFromName(note.getName());
        String host = NodeUtils.getHostFromName(note.getName());

        waitForFlush();

        reportsDownloader.setReportResultTypeDir("failed");
        reportsDownloader.downloadLastFailed(type, host, note.getName());

        failed = true;
        notifications++;
    }

    /**
     * Tests whether the test was failed
     * @return true if failed or false otherwise
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Tests whether the test was successful
     * @return true if successful or false otherwise
     */
    public abstract boolean isSuccessful();

    public boolean isCompleted() {
        if (isFailed()) {
            return true;
        }

        return isSuccessful();

    }

    /**
     * Returns the number of maestro notifications received
     * @return the number of maestro notifications received
     */
    public int getNotifications() {
        return notifications;
    }

    /**
     * Reset the number of maestro notifications received
     */
    public void resetNotifications() {
        this.notifications = 0;
    }
}
