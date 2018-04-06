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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(ReportsDownloader.class);

    private static final String SENDER_HOST_TYPE = "sender";
    private static final String RECEIVER_HOST_TYPE = "receiver";
    private static final String INSPECTOR_HOST_TYPE = "inspector";

    private final String baseDir;
    private String reportTypeDir;
    private int testNum;


    public ReportsDownloader(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }

    public void setReportResultTypeDir(String reportTypeDir) {
        this.reportTypeDir = reportTypeDir;
    }


    private String buildDir(final String address, final String hostType) {
        String host = URLUtils.getHostnameFromURL(address);

        // <basedir>/sender/failed/0/
        return baseDir + File.separator + hostType + File.separator + reportTypeDir + File.separator +
                Integer.toString(testNum) + File.separator + host;
    }

    private void downloadReport(final String targetURL, final String hostType) throws ResourceExchangeException {
        final String destinationDir = buildDir(targetURL, hostType);

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

    public void downloadLastSuccessful(final String type, final String host) {
        try {
            ReportResolver reportResolver = null;

            if (type.equals(SENDER_HOST_TYPE)) {
                reportResolver = new SenderReportResolver();
            }

            if (type.equals(RECEIVER_HOST_TYPE)) {
                reportResolver = new ReceiverReportResolver();
            }

            if (type.equals(INSPECTOR_HOST_TYPE)) {
                reportResolver = new InspectorReportResolver();
            }

            List<String> files = reportResolver.getSuccessFiles(host);
            for (String url : files) {
                downloadReport(url, type);
            }
         }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }

    public void downloadLastFailed(final String type, final String host) {
        try {
            ReportResolver reportResolver = null;

            if (type.equals(SENDER_HOST_TYPE)) {
                reportResolver = new SenderReportResolver();
            }

            if (type.equals(RECEIVER_HOST_TYPE)) {
                reportResolver = new ReceiverReportResolver();
            }

            if (type.equals(INSPECTOR_HOST_TYPE)) {
                reportResolver = new InspectorReportResolver();
            }

            List<String> files = reportResolver.getFailedFiles(host);
            for (String url : files) {
                downloadReport(url, type);
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }


    public void downloadAny(final String host, final String testNumber) {

        try {
            SenderReportResolver senderReportResolver = new SenderReportResolver();

            try {
                List<String> files = senderReportResolver.getTestFiles(host, testNumber);
                for (String url : files) {
                    downloadReport(url, SENDER_HOST_TYPE);
                }
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host );
                    throw e;
                }
            }

            ReceiverReportResolver receiverReportResolver = new ReceiverReportResolver();

            try {
                List<String> files = receiverReportResolver.getTestFiles(host, testNumber);
                for (String url : files) {
                    downloadReport(url, RECEIVER_HOST_TYPE);
                }
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host );
                    throw e;
                }
            }

            InspectorReportResolver inspectorReportResolver = new InspectorReportResolver();

            try {
                List<String> files = inspectorReportResolver.getTestFiles(host, testNumber);
                for (String url : files) {
                    downloadReport(url, INSPECTOR_HOST_TYPE);
                }
            }
            catch (ResourceExchangeException e) {
                if (e.getCode() != HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Resource {} not found at {} ", e.getUrl(), host );
                    throw e;
                }
            }
        }
        catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }


}
