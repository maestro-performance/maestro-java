package org.maestro.client.exchange;

public class MaestroProcessedInfo {
    private int noteCount;
    private int responseCount = 0;
    private int notificationCount = 0;
    private int requestCount = 0;

    public MaestroProcessedInfo(int noteCount) {
        this.noteCount = noteCount;
    }

    public int getNoteCount() {
        return noteCount;
    }
    public int getResponseCount() {
        return responseCount;
    }

    public void incrementResponseCount() {
        responseCount++;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void incrementNotificationCount() {
        notificationCount++;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incrementRequestCount() {
        requestCount++;
    }
}
