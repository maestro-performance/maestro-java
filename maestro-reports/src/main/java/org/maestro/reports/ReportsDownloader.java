/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.reports;

import org.maestro.common.URLUtils;
import org.maestro.contrib.utils.Downloader;
import org.maestro.contrib.utils.resource.exceptions.ResourceExchangeException;
import org.apache.http.HttpStatus;
import org.maestro.reports.organizer.DefaultOrganizer;
import org.maestro.reports.organizer.Organizer;
import org.maestro.reports.organizer.TestTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A reports downloader that collects the reports after the test is complete or on demand via CLI
 */
public class ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ReportsDownloader.class);

    private static final String SENDER_HOST_TYPE = "sender";
    private static final String RECEIVER_HOST_TYPE = "receiver";
    private static final String INSPECTOR_HOST_TYPE = "inspector";

    private Map<String, ReportResolver> resolverMap = new HashMap<>();

    private final Organizer organizer;

    /**
     * Constructor
     * @param baseDir
     */
    public ReportsDownloader(final String baseDir) {
        this.organizer = new DefaultOrganizer(baseDir);

        resolverMap.put(SENDER_HOST_TYPE, new SenderReportResolver());
        resolverMap.put(RECEIVER_HOST_TYPE, new ReceiverReportResolver());
        resolverMap.put(INSPECTOR_HOST_TYPE, new InspectorReportResolver());
    }

    public ReportsDownloader(final Organizer organizer) {
        this.organizer = organizer;

        resolverMap.put(SENDER_HOST_TYPE, new SenderReportResolver());
        resolverMap.put(RECEIVER_HOST_TYPE, new ReceiverReportResolver());
        resolverMap.put(INSPECTOR_HOST_TYPE, new InspectorReportResolver());
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    /**
     * Add a new report resolver with the given host type
     * @param hostType the host type
     * @param reportResolver the report resolver to use for the host type
     */
    public void addReportResolver(final String hostType, final ReportResolver reportResolver) {
        resolverMap.put(hostType, reportResolver);
    }


//    private String buildDir(final String address, final String hostType) {
//        String host = URLUtils.getHostnameFromURL(address);
//
//        // <basedir>/sender/failed/0/
//        return baseDir + File.separator + hostType + File.separator + reportTypeDir + File.separator +
//                Integer.toString(testNum) + File.separator + host;
//    }

    private void downloadReport(final String targetURL, final String hostType) throws ResourceExchangeException {
        final String destinationDir = organizer.organize(targetURL, hostType);

        if (logger.isDebugEnabled()) {
            logger.debug("Downloading file {} to {}", targetURL, destinationDir);
        }
        else {
            logger.info("Downloading file {}", targetURL);
        }

        try {
            Downloader.download(targetURL, destinationDir, true);
        }
        catch (ResourceExchangeException re) {
            if (re.getCode() == HttpStatus.SC_NOT_FOUND) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Maybe it's still flushing the data ... who knows. We can wait a bit and try again
                Downloader.download(targetURL, destinationDir, true);
            }
            else {
                throw re;
            }
        }
    }

    /**
     * Download files from the peers when a test is successful
     * @param type the type of the peer (sender, receiver, inspector, etc)
     * @param host the host to download the files from
     */
    public void downloadLastSuccessful(final String type, final String host) {
        try {
            ReportResolver reportResolver = resolverMap.get(type);

            List<String> files = reportResolver.getSuccessFiles(host);
            for (String url : files) {
                downloadReport(url, type);
            }
         }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Download files from the peers when a test failed
     * @param type the type of the peer (sender, receiver, inspector, etc)
     * @param host the host to download the files from
     */
    public void downloadLastFailed(final String type, final String host) {
        try {
            ReportResolver reportResolver = resolverMap.get(type);

            List<String> files = reportResolver.getFailedFiles(host);
            for (String url : files) {
                downloadReport(url, type);
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }


    private void downloadAny(final ReportResolver reportResolver, final String host, final String testNumber) {
        try {
            List<String> files = reportResolver.getTestFiles(host, testNumber);
            for (String url : files) {
                downloadReport(url, SENDER_HOST_TYPE);
            }
        }
        catch (ResourceExchangeException e) {
            if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                logger.warn("Resource {} not found at {} ", e.getUrl(), host );
            }
            else {
                logger.error("Unable to download files from " + e.getUrl());
            }
        }
    }


    /**
     * Download files from the peers
     * @param host the host to download the files from
     * @param testNumber the test execution number from the peer or one of the links (last, lastSuccessful, lastFailed)
     */
    public void downloadAny(final String host, final String testNumber) {
        resolverMap.values().forEach(value -> downloadAny(value, host, testNumber));
    }
}
