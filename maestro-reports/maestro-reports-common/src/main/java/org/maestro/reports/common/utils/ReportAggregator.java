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
 *
 */

package org.maestro.reports.common.utils;

import org.HdrHistogram.*;
import org.maestro.common.Role;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.io.data.common.exceptions.InvalidRecordException;
import org.maestro.common.io.data.writers.BinaryRateUpdater;
import org.maestro.common.io.data.writers.LatencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ReportAggregator {
    private static final Logger logger = LoggerFactory.getLogger(ReportAggregator.class);
    private final Map<String, List<File>> aggregatables = new HashMap<>();

    private final File baseDir;

    public ReportAggregator(final String baseDir) {
        this(new File(baseDir));
    }

    public ReportAggregator(final File baseDir) {
        this.baseDir = baseDir;
    }

    private boolean isAggregatable(final String name) {
        if (name == null) {
            return false;
        }

        String knownAggregatableFiles[] = {"receiver.dat", "sender.dat", "receiverd-latency.hdr"};

        for (String fileName : knownAggregatableFiles) {
            if (fileName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private void sort(final String location) {
        final File reportDir = new File(location);

        if (!reportDir.isDirectory()) {
            logger.error("Location {} is not a directory is will be ignored for aggregation",
                    reportDir);

            return;
        }

        if (!reportDir.exists()) {
            logger.error("Location {} does not exist and will be ignored for aggregation",
                    reportDir);

            return;
        }

        final String AGGREGATED_REPORT_DIRNAME = "aggregated";

        if (reportDir.getPath().contains(AGGREGATED_REPORT_DIRNAME)) {
            logger.error("Location {} is already aggregated and will be ignored for aggregation",
                    reportDir);

            return;
        }

        final File reportFiles[] = reportDir.listFiles();
        for (File reportFile : reportFiles) {
            if (isAggregatable(reportFile.getName())) {
                List<File> files = aggregatables.get(reportFile.getName());

                if (files == null) {
                    files = new LinkedList<>();
                }

                files.add(reportFile);

                aggregatables.put(reportFile.getName(), files);
            }
        }
    }

    private void aggregate(final String name, final List<File> files) {
        if (name.equals("receiver.dat")) {
            try {
                aggregateRate(files, Role.RECEIVER);
            } catch (MaestroException | IOException e) {
                logger.error("Unable to aggregate receiver rate files: {}", e.getMessage(), e);
            }
        }

        if (name.equals("sender.dat")) {
            try {
                aggregateRate(files, Role.SENDER);
            } catch (MaestroException | IOException e) {
                logger.error("Unable to aggregate sender rate files: {}", e.getMessage(), e);
            }
        }

        if (name.equals("receiverd-latency.hdr")) {
            try {
                aggregateLatencies(files);
            } catch (MaestroException | IOException e) {
                logger.error("Unable to aggregate receiver latency files: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Aggregates the list of files into the previously setup location
     * @param locations the report file locations
     */
    public void aggregate(final List<String> locations) {

        logger.info("Building the file list to be processed by the report aggregator");

        locations.forEach(this::sort);

        aggregatables.forEach(this::aggregate);
    }


    private void aggregateRate(List<File> currentReports, Role role) throws IOException {

        try (BinaryRateUpdater binaryRateUpdater = BinaryRateUpdater.get(role, baseDir)) {
            for (File currentReport : currentReports) {
                logger.info("Producing aggregated report for: {}", currentReport);

                try {
                    BinaryRateUpdater.joinFile(binaryRateUpdater, currentReport);
                }
                catch (InvalidRecordException e) {
                    logger.warn("File {} contains invalid records and will be ignored", currentReport);
                }
            }
        }
    }

    private void aggregateLatencies(List<File> currentReports) throws IOException {
        Histogram aggregatedHistogram = new Histogram(3);

        File destFile = new File(baseDir, "receiverd-latency.hdr");

        try (LatencyWriter latencyWriter = new LatencyWriter(destFile)) {

            for (File currentReport : currentReports) {
                logger.info("Producing aggregated report for : {}", currentReport);
                joinHistograms(aggregatedHistogram, currentReport);
            }

            latencyWriter.outputIntervalHistogram(aggregatedHistogram);
        }
    }

    private void joinHistograms(Histogram dest, File sourceFile) throws FileNotFoundException {
        HistogramLogReader logReader = new HistogramLogReader(sourceFile);

        while (logReader.hasNext()) {
            EncodableHistogram eh = logReader.nextIntervalHistogram();
            if (eh == null) {
                logger.warn("Unable to aggregate an histogram from file {} because it is empty", sourceFile);

                break;
            }

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
