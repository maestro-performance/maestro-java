package net.orpiske.mpt.common.client;

/**
 * Provides an interface that can be used by peers, workers, etc to publish
 * safe maestro responses and notifications
 */
@SuppressWarnings("unused")
public interface MaestroReceiver {

    /**
     * Publishes a ping response that takes into account a giver number of
     * elapsed seconds/microseconds
     * @param sec Epoch seconds
     * @param uSec Microseconds within the second
     */
    void pingResponse(long sec, long uSec);

    /**
     * Publishes a OK reply in the maestro broker
     */
    void replyOk();

    /**
     * Publishes an internal error reply in the Maestro broker
     */
    void replyInternalError();

    /**
     * Publishes a test success notification message in the broker
     * @param message payload message
     */
    void notifySuccess(final String message);

    /**
     * Publishes a test failure notification message in the broker
     * @param message payload message
     */
    void notifyFailure(final String message);

    /**
     * Publishes an abnormal disconnect notification message in the broker
     */
    void abnormalDisconnect();
}
