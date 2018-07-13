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

import org.HdrHistogram.Histogram;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.test.TestProperties;
import org.maestro.common.writers.LatencyWriter;
import org.maestro.reports.data.rate.RateToHistogram;
import org.maestro.reports.files.MptReportFile;
import org.maestro.reports.files.ReportFile;
import org.maestro.reports.node.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
            logger.error("Failed to generate aggreated report", e);
            throw new RuntimeException(e);
        }
    }

    private void aggregateSet(File currentTestNumDir, Set<MptReportFile> currentReports) throws IOException {
        logger.info("Producing aggregated report for directory : {} ({} report(s))", currentTestNumDir, currentReports.size());

        CSVPrinter csvPrinter = null;
        NodeType nodeType = null;
        File aggregatedReport = null;
        File aggregatedReportRoot = new File(currentTestNumDir, AGGREGATED_REPORT_DIRNAME);
        Properties props = null;
        try {
            for (MptReportFile currentReport : currentReports) {
                logger.debug("Processing {}", currentReport.getSourceFile());

                final String name = currentReport.getSourceFile().getName();
                final boolean compressed = name.endsWith(".gz");
                final String fileName = currentReport.getSourceFile().getPath();
                final File file = new File(fileName);

                try (Reader in = getReader(file, compressed)) {
                    CSVParser parser = CSVFormat.RFC4180
                            .withCommentMarker('#')
                            .withFirstRecordAsHeader()
                            .withQuote('"')
                            .withQuoteMode(QuoteMode.NON_NUMERIC).parse(in);

                    String[] headers = getCsvHeaders(parser);

                    if (csvPrinter == null) {
                        aggregatedReportRoot.mkdirs();

                        aggregatedReport = new File(aggregatedReportRoot, name);
                        Writer reportWriter = getWriter(aggregatedReport, compressed);

                        csvPrinter = new CSVPrinter(reportWriter, CSVFormat.RFC4180
                                .withQuote('"')
                                .withQuoteMode(QuoteMode.NON_NUMERIC)
                                .withHeader(headers));
                    } else {
                        String[] outputHeaders = getCsvHeaders(parser);

                        if (!Arrays.equals(outputHeaders, headers)) {
                            throw new MaestroException(String.format("Header row %s from CSV report '%s' does not match" +
                                            " the header row already established for this aggregated report : %s",
                                    Arrays.toString(headers), name, Arrays.toString(outputHeaders)));
                        }
                    }

                    csvPrinter.printRecords(parser);

                    nodeType = currentReport.getNodeType();

                    if (props == null) {
                        File testProps = new File(currentReport.getReportDir(), TestProperties.FILENAME);
                        if (testProps.isFile()) {
                            props = new Properties();
                            try (InputStream is = new FileInputStream(testProps)) {
                                props.load(is);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new IOException(String.format("Failed to aggregate '%s'", fileName), e);
                }
            }

        } finally {
            if (csvPrinter != null) {
                csvPrinter.close();
            }
        }

        if (nodeType == NodeType.RECEIVER) {
            createHistogram(aggregatedReportRoot, aggregatedReport);
        }

        if (props != null) {
            File f = new File(aggregatedReportRoot, TestProperties.FILENAME);
            try (OutputStream os = new FileOutputStream(f)) {
                props.store(os, "");
            }
        }

    }

    private void createHistogram(File aggregateReportRoot, File aggregatedReport) throws IOException {
        File aggregatedHistogram = new File(aggregateReportRoot, "receiverd-latency.hdr");
        logger.info("Creating aggregated histogram : {}", aggregatedHistogram);

        try (LatencyWriter writer = new LatencyWriter(aggregatedHistogram)) {
            final Histogram histogram = new Histogram(3);
            String fileName = aggregatedReport.getPath();
            final boolean compressed = fileName.endsWith(".gz");
            final File file = new File(fileName);

            try (Reader in = getReader(file, compressed)) {
                RateToHistogram.rebuildHistogram(in, histogram);
            }
            writer.outputLegend(0);
            writer.outputIntervalHistogram(histogram);
        }
    }

    private static Reader getReader(File file, boolean compressed) throws IOException {
        InputStream is = compressed ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file);
        return new InputStreamReader(new BufferedInputStream(is));
    }

    private Writer getWriter(File file, boolean compressed) throws IOException {
        final OutputStream os = compressed ? new GZIPOutputStream(new FileOutputStream(file)) : new FileOutputStream(file);
        return new OutputStreamWriter(new BufferedOutputStream(os));
    }

    private String[] getCsvHeaders(CSVParser parser) {
        List<String> orderedHeaders = new ArrayList<>(parser.getHeaderMap().keySet());
        return orderedHeaders.toArray(new String[orderedHeaders.size()]);
    }
}
