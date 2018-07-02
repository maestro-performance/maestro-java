package org.maestro.reports.downloaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.LocationType;
import org.maestro.client.notes.LogResponse;
import org.maestro.common.HostTypes;
import org.maestro.common.NodeUtils;
import org.maestro.common.URLUtils;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.contrib.utils.digest.Sha1Digest;
import org.maestro.reports.ReceiverReportResolver;
import org.maestro.reports.ReportResolver;
import org.maestro.reports.SenderReportResolver;
import org.maestro.reports.organizer.NodeOrganizer;
import org.maestro.reports.organizer.Organizer;
import org.maestro.client.Maestro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrokerDownloader implements ReportsDownloader {
    private static final Logger logger = LoggerFactory.getLogger(BrokerDownloader.class);

    private static class DownloadCallback implements MaestroNoteCallback {
        private static final Logger logger = LoggerFactory.getLogger(DownloadCallback.class);
        private NodeOrganizer organizer;
        private Sha1Digest digest = new Sha1Digest();

        DownloadCallback(final NodeOrganizer organizer) {
            this.organizer = organizer;
        }

        private void save(final LogResponse logResponse) {
            String type = NodeUtils.getTypeFromName(logResponse.getName());
            String destDir = organizer.organize(logResponse.getName(), type);

            switch (logResponse.getLocationType()) {
                case LAST_SUCCESS: {
                    organizer.setResultType("success");
                    break;
                }
                default: {
                    organizer.setResultType("failed");
                    break;
                }
            }

            File outFile = new File(destDir, logResponse.getFileName());

            logger.info("Saving file {} to {}", logResponse.getFileName(), outFile);
            if (!outFile.exists()) {
                try {
                    FileUtils.forceMkdirParent(outFile);
                } catch (IOException e) {
                    logger.error("Unable to create parent directories: {}", e.getMessage(), e);
                }
            }

            try (FileOutputStream fo = new FileOutputStream(outFile)) {
                IOUtils.copy(logResponse.getLogData(), fo);
            } catch (FileNotFoundException e) {
                logger.error("Unable to save the file: {}", e.getMessage(), e);
            } catch (IOException e) {
                logger.error("Unable to save the file due to I/O error: {}", e.getMessage(), e);
            }

            verify(logResponse, outFile);
        }

        private void verify(LogResponse logResponse, File outFile) {
            if (logResponse.getFileHash() != null && !logResponse.getFileHash().isEmpty()) {
                try {
                    logger.info("Verifying SHA-1 hash for file {}", outFile);
                    if (!digest.verify(outFile.getPath(), logResponse.getFileHash())) {
                        logger.error("The SHA-1 hash for file {} does not match the expected one {}",
                                outFile.getPath(), logResponse.getFileHash());
                    }
                } catch (IOException e) {
                    logger.error("Unable to verify the hash for file {}: {}", outFile.getName(),
                            e.getMessage());
                }
            }
            else {
                logger.warn("The peer did not set up a hash for file {}", logResponse.getFileName());
            }
        }

        @Override
        public void call(MaestroNote note) {
            if (note instanceof LogResponse) {
                LogResponse logResponse = (LogResponse) note;
                if (logResponse.getLocationType() == LocationType.LAST_FAILED) {
                    logger.info("About to download last failed reports");
                }
                else {
                    logger.info("About to download last successful reports");
                }
                save(logResponse);
            }
        }
    }

    private Map<String, ReportResolver> resolverMap = new HashMap<>();
    private Maestro maestro;

    private final NodeOrganizer organizer;

    public BrokerDownloader(final Maestro maestro, final String baseDir) {
        this(maestro, new NodeOrganizer(baseDir));
    }

    public BrokerDownloader(final Maestro maestro, final NodeOrganizer organizer) {
        this.maestro = maestro;
        this.organizer = organizer;

        resolverMap.put(HostTypes.SENDER_HOST_TYPE, new SenderReportResolver());
        resolverMap.put(HostTypes.RECEIVER_HOST_TYPE, new ReceiverReportResolver());

        maestro.getCollector().getCallbacks().add(new DownloadCallback(organizer));
    }

    @Override
    public Organizer getOrganizer() {
        return organizer;
    }

    @Override
    public void addReportResolver(final String hostType, final ReportResolver reportResolver) {
        resolverMap.put(hostType, reportResolver);
    }

    private String getDataServerHost(final String dataServer) throws MalformedURLException {
        final URL url = new URL(dataServer);

        return url.getHost();
    }


    private void download(final String type, final String dataServer, LocationType locationType) {
        final String host = URLUtils.getHostnameFromURL(dataServer);
        final String topic = MaestroTopics.peerTopic(type, host);

        maestro.logRequest(topic, locationType , null);
    }

    @Override
    public void downloadLastSuccessful(final String type, final String dataServer) {
        download(type, dataServer, LocationType.LAST_SUCCESS);
    }


    @Override
    public void downloadLastFailed(final String type, final String dataServer) {
        download(type, dataServer, LocationType.LAST_FAILED);
    }

    @Override
    public void downloadAny(final String type, final String dataServer, final String testNumber) {
        try {
            final String host = getDataServerHost(dataServer);
            final String topic = MaestroTopics.peerTopic(type, host);

            maestro.logRequest(topic, LocationType.ANY , testNumber);
        } catch (MalformedURLException e) {
            logger.error("Unable to parse data server URL {}: {}", dataServer, e.getMessage());
        }
    }
}
