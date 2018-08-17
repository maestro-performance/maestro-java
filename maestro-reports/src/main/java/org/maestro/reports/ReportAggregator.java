/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.reports;

import org.HdrHistogram.*;
import org.apache.commons.io.FileUtils;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.io.data.writers.BinaryRateUpdater;
import org.maestro.common.test.TestProperties;
import org.maestro.common.io.data.writers.LatencyWriter;
import org.maestro.reports.files.MptReportFile;
import org.maestro.reports.files.ReportFile;
import org.maestro.reports.node.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportAggregator {
    private static final Logger logger = LoggerFactory.getLogger(ReportAggregator.class);
    static final String AGGREGATED_REPORT_DIRNAME = "aggregated";

    private final String path;

    public ReportAggregator(String path) {
        this.path = path;
    }

    public void aggregate() {
        File baseDir = new File(path);
        logger.info("Building the file list to be processed by the report aggregator");
        final ReportDirectoryWalker processor = new ReportDirectoryWalker(path);

        final List<ReportFile> fileList = processor.generate(baseDir);

        List<MptReportFile> reports = fileList.stream()
                .filter(MptReportFile.class::isInstance)
                .map(MptReportFile.class::cast)
                .filter(item -> !item.getReportDir().getName().startsWith(AGGREGATED_REPORT_DIRNAME))
                .collect(Collectors.toList());
        logger.info("There are {} files to be processed", fileList.size());

        try {
            Set<MptReportFile> currentReports = new HashSet<>();
            File currentTestNumDir = null;
            for (MptReportFile report : reports) {
                File reportTestNumDir = report.getReportDir().getParentFile();

                if (!reportTestNumDir.equals(currentTestNumDir) && currentTestNumDir != null) {
                    aggregateSet(currentTestNumDir, currentReports);
                    currentReports.clear();
                    currentReports.add(report);
                }
                currentReports.add(report);
                currentTestNumDir = reportTestNumDir;
            }

            if (!currentReports.isEmpty()) {
                aggregateSet(currentTestNumDir, currentReports);
            }
        } catch (IOException e) {
            logger.error("Failed to generate aggregated report", e);
            throw new RuntimeException(e);
        }
    }

    private void aggregateSet(File currentTestNumDir, Set<MptReportFile> currentReports) throws IOException {
        logger.info("Producing aggregated report for directory : {} ({} report(s))", currentTestNumDir, currentReports.size());

        NodeType nodeType;
        File aggregatedReportRoot = new File(currentTestNumDir, AGGREGATED_REPORT_DIRNAME);
        Properties props = null;

        BinaryRateUpdater binaryRateUpdater = null;
        Histogram aggregatedHistogram = null;

        try {
            for (MptReportFile currentReport : currentReports) {
                nodeType = currentReport.getNodeType();

                if (binaryRateUpdater == null) {
                    binaryRateUpdater = getBinaryRateUpdater(nodeType, aggregatedReportRoot);
                }

                logger.debug("Processing {}", currentReport.getSourceFile());

                final String fileName = currentReport.getSourceFile().getPath();

                BinaryRateUpdater.joinFile(binaryRateUpdater, new File(fileName));

                if (nodeType == NodeType.RECEIVER) {
                    if (aggregatedHistogram == null) {
                        aggregatedHistogram = new Histogram(3);
                    }

                    joinHistograms(aggregatedHistogram, currentReport.getReportDir());
                }

                if (props == null) {
                    File testProps = new File(currentReport.getReportDir(), TestProperties.FILENAME);
                    if (testProps.isFile()) {
                        props = new Properties();
                        try (InputStream is = new FileInputStream(testProps)) {
                            props.load(is);
                        }
                    }
                }
            }

        } finally {
            if (binaryRateUpdater != null) {
                binaryRateUpdater.close();
            }

            if (aggregatedHistogram != null) {
                LatencyWriter latencyWriter = new LatencyWriter(new File(aggregatedReportRoot, "receiverd-latency.hdr"));

                latencyWriter.outputIntervalHistogram(aggregatedHistogram);
                latencyWriter.close();
            }
        }

        if (props != null) {
            File f = new File(aggregatedReportRoot, TestProperties.FILENAME);
            try (OutputStream os = new FileOutputStream(f)) {
                props.store(os, "");
            }
        }

    }

    private BinaryRateUpdater getBinaryRateUpdater(NodeType nodeType, File aggregatedReportRoot) throws IOException {
        File output;
        if (nodeType == NodeType.RECEIVER) {
            output = new File(aggregatedReportRoot, "receiver.dat");
        } else {
            output = new File(aggregatedReportRoot, "sender.dat");
        }

        FileUtils.forceMkdirParent(output);
        return new BinaryRateUpdater(output, false);
    }

    private void joinHistograms(Histogram dest, File sourceDir) throws FileNotFoundException {
        HistogramLogReader logReader = new HistogramLogReader(new File(sourceDir, "receiverd-latency.hdr"));

        while (logReader.hasNext()) {
            EncodableHistogram eh = logReader.nextIntervalHistogram();
            if (eh instanceof AbstractHistogram) {
                AbstractHistogram ah = (AbstractHistogram) eh;

                dest.add(ah);
            }
            else {
                // Maestro-generated histograms should always be AbstractHistogram, so this shouldn't happen
                throw new MaestroException("The histogram type does not allow it to be aggregated");
            }
        }
    }
}
