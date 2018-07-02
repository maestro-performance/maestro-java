package org.maestro.reports.downloaders;

import org.apache.http.HttpStatus;
import org.maestro.common.HostTypes;
import org.maestro.contrib.utils.Downloader;
import org.maestro.contrib.utils.resource.exceptions.ResourceExchangeException;
import org.maestro.reports.ReceiverReportResolver;
import org.maestro.reports.ReportResolver;
import org.maestro.reports.SenderReportResolver;
import org.maestro.reports.organizer.DefaultOrganizer;
import org.maestro.reports.organizer.Organizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDownloader implements ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDownloader.class);

    private Map<String, ReportResolver> resolverMap = new HashMap<>();

    private final Organizer organizer;
    private long lastDownloadedTime;

    /**
     * Constructor
     * @param baseDir
     */
    public DefaultDownloader(final String baseDir) {
        this.organizer = new DefaultOrganizer(baseDir);

        resolverMap.put(HostTypes.SENDER_HOST_TYPE, new SenderReportResolver());
        resolverMap.put(HostTypes.RECEIVER_HOST_TYPE, new ReceiverReportResolver());
    }

    public DefaultDownloader(final Organizer organizer) {
        this.organizer = organizer;

        resolverMap.put(HostTypes.SENDER_HOST_TYPE, new SenderReportResolver());
        resolverMap.put(HostTypes.RECEIVER_HOST_TYPE, new ReceiverReportResolver());
    }

    public Organizer getOrganizer() {
        return organizer;
    }


    public void addReportResolver(final String hostType, final ReportResolver reportResolver) {
        resolverMap.put(hostType, reportResolver);
    }


    private void downloadReport(final String targetURL, final String hostType) throws ResourceExchangeException {
        final String destinationDir = organizer.organize(targetURL, hostType);

        if (logger.isDebugEnabled()) {
            logger.debug("Downloading the {} report file {} to {}", hostType, targetURL, destinationDir);
        }
        else {
            logger.info("Downloading the {} report file {}", hostType, targetURL);
        }

        boolean downloaded = false;
        int repeat = 10;

        do {

            try {
                Downloader.download(targetURL, destinationDir, true);
                downloaded = true;
            } catch (ResourceExchangeException re) {
                if (re.getCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Remote resource not found or not available yet. Retrying in 5 seconds");
                    try {
                        Thread.sleep(5000);
                        repeat--;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw re;
                }
            }
        } while (!downloaded && repeat > 0);
    }

    /**
     * Download files from the peers when a test is successful
     * @param type the type of the peer (sender, receiver, inspector, etc)
     * @param host the host to download the files from
     */
    public void downloadLastSuccessful(final String type, final String host) {
        ReportResolver reportResolver = resolverMap.get(type);

        List<String> files = reportResolver.getSuccessFiles(host);
        for (String url : files) {
            try {
                downloadReport(url, type);
            } catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host);
                }
                else {
                    logger.error("Error: {}", e.getMessage(), e);
                }
            }
        }

        lastDownloadedTime = System.currentTimeMillis();
    }

    /**
     * Download files from the peers when a test failed
     * @param type the type of the peer (sender, receiver, inspector, etc)
     * @param host the host to download the files from
     */
    public void downloadLastFailed(final String type, final String host) {
        ReportResolver reportResolver = resolverMap.get(type);

        List<String> files = reportResolver.getFailedFiles(host);
        for (String url : files) {
            try {
                downloadReport(url, type);
            } catch (ResourceExchangeException e) {
                if (e.getCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host);
                }
                else {
                    logger.error("Error: {}", e.getMessage(), e);
                }
            }
        }

        lastDownloadedTime = System.currentTimeMillis();
    }


    private void downloadAny(final ReportResolver reportResolver, final String host, final String testNumber) {
        List<String> files = reportResolver.getTestFiles(host, testNumber);
        for (String url : files) {
            try {
                downloadReport(url, HostTypes.SENDER_HOST_TYPE);
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host);
                }
                else {
                    logger.error("Unable to download files from {}", e.getUrl());
                }
            }
        }
    }


    /**
     * Download files from the peers
     * @param host the host to download the files from
     * @param testNumber the test execution number from the peer or one of the links (last, lastSuccessful, lastFailed)
     */
    public void downloadAny(final String type, final String host, final String testNumber) {
        ReportResolver reportResolver = resolverMap.get(type);
        if (reportResolver != null) {
            downloadAny(reportResolver, host, testNumber);
        }
        else {
            logger.warn("There is no report resolver registered for host type {}", type);
        }

        lastDownloadedTime = System.currentTimeMillis();
    }

    @Override
    public void waitForComplete() {
        // NO-OP
    }
}
