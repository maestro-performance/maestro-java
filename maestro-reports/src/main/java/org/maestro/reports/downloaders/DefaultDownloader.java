package org.maestro.reports.downloaders;

import org.apache.http.HttpStatus;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.common.Role;
import org.maestro.contrib.utils.Downloader;
import org.maestro.contrib.utils.resource.exceptions.ResourceExchangeException;
import org.maestro.reports.ReceiverReportResolver;
import org.maestro.reports.ReportResolver;
import org.maestro.reports.SenderReportResolver;
import org.maestro.reports.organizer.DefaultOrganizer;
import org.maestro.reports.organizer.Organizer;
import org.maestro.reports.organizer.ResultStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDownloader implements ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDownloader.class);

    private final Map<Role, ReportResolver> resolverMap = new HashMap<>();

    private final Organizer organizer;
    private long lastDownloadedTime;

    /**
     * Constructor
     * @param baseDir the base directory for saving the reports
     */
    public DefaultDownloader(final String baseDir) {
        this.organizer = new DefaultOrganizer(baseDir);

        resolverMap.put(Role.SENDER, new SenderReportResolver());
        resolverMap.put(Role.RECEIVER, new ReceiverReportResolver());
    }

    /**
     * Constructor
     * @param organizer the report organizer to use
     */
    public DefaultDownloader(final Organizer organizer) {
        this.organizer = organizer;

        resolverMap.put(Role.SENDER, new SenderReportResolver());
        resolverMap.put(Role.RECEIVER, new ReceiverReportResolver());
    }

    public Organizer getOrganizer() {
        return organizer;
    }


    public void addReportResolver(final Role role, final ReportResolver reportResolver) {
        resolverMap.put(role, reportResolver);
    }


    private void downloadReport(final String targetURL, final PeerInfo peerInfo) throws ResourceExchangeException {
        final String destinationDir = organizer.organize(peerInfo);

        if (logger.isDebugEnabled()) {
            logger.debug("Downloading the {} report file {} to {}", peerInfo.getRole(),
                    targetURL, destinationDir);
        }
        else {
            logger.info("Downloading the {} report file {}", peerInfo.getRole(), targetURL);
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
     * @param peerInfo the peer information
     */
    @Override
    public synchronized void downloadLastSuccessful(final String id, final PeerInfo peerInfo) {
        ReportResolver reportResolver = resolverMap.get(peerInfo.getRole());

        getOrganizer().setResultType(ResultStrings.SUCCESS);

        List<String> files = reportResolver.getSuccessFiles(peerInfo.peerHost());
        for (String url : files) {
            try {
                downloadReport(url, peerInfo);
            } catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), peerInfo.peerHost());
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
     * @param peerInfo the peer information
     */
    @Override
    public void downloadLastFailed(final String id, final PeerInfo peerInfo) {
        ReportResolver reportResolver = resolverMap.get(peerInfo.getRole());

        getOrganizer().setResultType(ResultStrings.FAILED);

        List<String> files = reportResolver.getFailedFiles(peerInfo.peerHost());
        for (String url : files) {
            try {
                downloadReport(url, peerInfo);
            } catch (ResourceExchangeException e) {
                if (e.getCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), peerInfo.peerHost());
                }
                else {
                    logger.error("Error: {}", e.getMessage(), e);
                }
            }
        }

        lastDownloadedTime = System.currentTimeMillis();
    }


    private void downloadAny(final ReportResolver reportResolver, final PeerInfo peerInfo, final String testNumber) {
        List<String> files = reportResolver.getTestFiles(peerInfo.peerHost(), testNumber);
        for (String url : files) {
            try {
                downloadReport(url, peerInfo);
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), peerInfo.peerHost());
                }
                else {
                    logger.error("Unable to download files from {}", e.getUrl());
                }
            }
        }
    }


    @Override
    public void downloadAny(final PeerInfo peerInfo, final String testNumber) {
        ReportResolver reportResolver = resolverMap.get(peerInfo.getRole());
        if (reportResolver != null) {
            downloadAny(reportResolver, peerInfo, testNumber);
        }
        else {
            logger.warn("There is no report resolver registered for host type {}", peerInfo.getRole());
        }

        lastDownloadedTime = System.currentTimeMillis();
    }

    @Override
    public void waitForComplete() {
        // NO-OP
    }
}
