package net.orpiske.mpt.maestro.client;

public interface MaestroReceiver {

    void pingResponse(long sec, long uSec);
    void replyOk();
    void replyInternalError();

    void notifySuccess(final String message);
    void notifyFailure(final String message);
    void abnormalDisconnect();
}
