package org.maestro.reports.downloaders;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Monitor;
import org.maestro.common.NonProgressingStaleChecker;
import org.maestro.common.StaleChecker;
import org.maestro.common.client.notes.LocationType;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.reports.organizer.DefaultOrganizer;
import org.maestro.reports.organizer.Organizer;
import org.maestro.client.Maestro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerDownloader implements ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(BrokerDownloader.class);

    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final Maestro maestro;

    private final Organizer organizer;
    private final DownloadCallback downloadCallback;
    private final Monitor<Object> monitor;

    public BrokerDownloader(final Maestro maestro, final String baseDir) {
        this(maestro, new DefaultOrganizer(baseDir));
    }

    public BrokerDownloader(final Maestro maestro, final Organizer organizer) {
        this.maestro = maestro;
        this.organizer = organizer;
        this.monitor = new Monitor<>(new Object());

        maestro.getCollector().subscribe(MaestroTopics.MAESTRO_LOGS_TOPIC, 0);

        downloadCallback = new DownloadCallback(organizer, this.monitor);
        maestro.getCollector().addCallback(downloadCallback);
    }

    @Override
    public Organizer getOrganizer() {
        return organizer;
    }

    private void download(final String id, final LocationType locationType) {
        final String topic = MaestroTopics.peerTopic(id);

        logger.debug("Sending log request to {}", topic);

        maestro.logRequest(topic, locationType , null);
    }

    @Override
    public void downloadLastSuccessful(final String id, final PeerInfo peerInfo) {
        download(id, LocationType.LAST_SUCCESS);
    }


    @Override
    public void downloadLastFailed(final String id, final PeerInfo peerInfo) {
        download(id, LocationType.LAST_FAILED);
    }

    @Override
    public void downloadAny(final String id, final String testNumber) {
        final String topic = MaestroTopics.peerTopic(id);

        logger.debug("Sending log request to {}", topic);

        maestro.logRequest(topic, LocationType.ANY , testNumber);
    }


    @Override
    public void waitForComplete() {
        int expiryTime = config.getInt("download.broker.expiry", 20);
        logger.info("Waiting for up to {} retries all the logs to arrive", expiryTime);

        StaleChecker staleChecker = new NonProgressingStaleChecker(expiryTime);

        do {
            if (staleChecker.isStale(downloadCallback.getDownloadCount())) {
                break;
            }

            try {
                logger.trace("Waiting for more files to arrive");
                monitor.doLock(1000);
            } catch (InterruptedException e) {
                logger.trace("Interrupted while waiting for logs to arrive");
                break;
            }
        } while(true);
    }
}
