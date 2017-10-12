package net.orpiske.mpt.test;

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

    private ReportsDownloader reportsDownloader;
    private AbstractTestProfile testProfile;

    private boolean failed = false;
    private int notifications = 0;

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
        logger.info("Elapsed time from {}: {} ms", note.getName(), note.getElapsed());
    }

    @Override
    protected void processNotifySuccess(TestSuccessfulNotification note) {
        logger.info("Test successful on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameters used: " + testProfile.toString());

        String type = note.getName().split("@")[0];
        String host = note.getName().split("@")[1];

        reportsDownloader.setReportTypeDir("success");
        reportsDownloader.downloadLastSuccessful(type, host, note.getName());

        notifications++;
    }

    @Override
    protected void processNotifyFail(TestFailedNotification note) {
        logger.info("Test failed on {} after {} executions", note.getName(),
                testProfile.getTestExecutionNumber());
        logger.info("Test parameter used");

        String type = note.getName().split("@")[0];
        String host = note.getName().split("@")[1];

        reportsDownloader.setReportTypeDir("failed");
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
    abstract boolean isSuccessful();

    public boolean isCompleted() {
        if (isFailed()) {
            return true;
        }

        if (isSuccessful()) {
            return true;
        }

        return false;
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
