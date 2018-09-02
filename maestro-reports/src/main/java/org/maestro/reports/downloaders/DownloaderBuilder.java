package org.maestro.reports.downloaders;

import org.maestro.client.Maestro;
import org.maestro.reports.organizer.Organizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to build the reports downloader
 */

public class DownloaderBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DownloaderBuilder.class);
    public static final String POOLED = "pooled-";

    private DownloaderBuilder() {}


    /**
     * Build a report downloader
     * @param name The name of the downloader ("broker" or "default")
     * @param maestro Maestro instance (required for "broker")
     * @param baseDir Directory where to save the files
     * @return The reports downloader
     */
    public static ReportsDownloader build(String name, final Maestro maestro, final String baseDir) {
        boolean pool = false;
        if (name != null && name.toLowerCase().startsWith(POOLED)) {
            name = name.substring(POOLED.length());
            pool = true;
        }

        final ReportsDownloader reportsDownloader;
        logger.debug("Using the broker report downloader");
        reportsDownloader = new BrokerDownloader(maestro, baseDir);

        return pool ? new PooledDownloaderDecorator(reportsDownloader) : reportsDownloader;
    }



    /**
     * Build a report downloader
     * @param name The name of the downloader ("broker" or "default")
     * @param maestro Maestro instance (required for "broker")
     * @param organizer Report directory organizer
     * @return The reports downloader
     */
    public static ReportsDownloader build(String name, final Maestro maestro, final Organizer organizer) {
        boolean pool = false;
        if (name != null && name.toLowerCase().startsWith(POOLED)) {
            name = name.substring(POOLED.length());
            pool = true;
        }

        final ReportsDownloader reportsDownloader;
        reportsDownloader = new BrokerDownloader(maestro, organizer);

        return pool ? new PooledDownloaderDecorator(reportsDownloader) : reportsDownloader;
    }
}
