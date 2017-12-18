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

package net.orpiske.mpt.reports;

import net.orpiske.mpt.utils.Downloader;
import net.orpiske.mpt.utils.contrib.resource.exceptions.ResourceExchangeException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ReportsDownloader.class);

    private static final String LAST_SUCCESSFUL_DIR = "lastSuccessful";
    private static final String LAST_FAILED_DIR = "lastFailed";

    private String baseDir;
    private String reportTypeDir;
    private int testNum;


    public ReportsDownloader(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }

    public void setReportTypeDir(String reportTypeDir) {
        this.reportTypeDir = reportTypeDir;
    }

    private String buildDir(String host) {
        // "/tmp/mpt/groovy/success/1/host
        return baseDir + File.separator + reportTypeDir + File.separator + Integer.toString(testNum) + File.separator
                + host;
    }

    private void downloadReport(String host, String reportSource, String name) throws ResourceExchangeException {
        String baseURL = "http://" + host + ":8000/" + reportSource + "/";
        String targetURL = baseURL + name;

        logger.info("Downloading file {}", targetURL);
        Downloader.download(targetURL, buildDir(host), true);
    }

    private void downloadSenderReports(String host, String reportSource) throws ResourceExchangeException {
        String name = "senderd-rate.csv.gz";

        downloadReport(host, reportSource, name);

        downloadReport(host, reportSource, "test.properties");
    }

    private void downloadReceiverReports(String host, String reportSource) throws ResourceExchangeException {
        String tpReport = "receiverd-rate.csv.gz";

        downloadReport(host, reportSource, tpReport);

        String latReport = "receiverd-latency.hdr";

        downloadReport(host, reportSource, latReport);


        downloadReport(host, reportSource, "test.properties");
    }

    private void downloadInspectorReports(String host, String reportSource) throws ResourceExchangeException {
        downloadReport(host, reportSource, "broker-jvm-inspector.csv.gz");
        downloadReport(host, reportSource, "test.properties");
        downloadReport(host, reportSource, "broker.properties");
    }

    public void downloadLastSuccessful(final String type, final String host, final String name) {
        try {
            if (type.equals("sender")) {
                downloadSenderReports(host, LAST_SUCCESSFUL_DIR);
            }

            if (type.equals("receiver")) {
                downloadReceiverReports(host, LAST_SUCCESSFUL_DIR);
            }

            if (type.equals("inspector")) {
                downloadInspectorReports(host, LAST_SUCCESSFUL_DIR);
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }

    public void downloadLastFailed(final String type, final String host, final String name) {
        try {
            if (type.equals("sender")) {
                downloadSenderReports(host, LAST_FAILED_DIR);
            }

            if (type.equals("receiver")) {
                downloadReceiverReports(host, LAST_FAILED_DIR);
            }

            if (type.equals("inspector")) {
                downloadInspectorReports(host, LAST_FAILED_DIR);
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }


    public void downloadAny(final String host, String resource) {
        try {
            try {
                downloadSenderReports(host, resource);
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", resource, host );
                    throw e;
                }
            }

            try {
                downloadReceiverReports(host, resource);
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", resource, host );
                    throw e;
                }
            }

            try {
                downloadInspectorReports(host, resource);
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", resource, host );
                    throw e;
                }
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }


}
