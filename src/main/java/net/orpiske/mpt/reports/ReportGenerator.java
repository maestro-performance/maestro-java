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

import net.orpiske.hhp.plot.HdrData;
import net.orpiske.hhp.plot.HdrLogProcessorWrapper;
import net.orpiske.hhp.plot.HdrPlotter;
import net.orpiske.hhp.plot.HdrReader;
import net.orpiske.mdp.plot.RateData;
import net.orpiske.mdp.plot.RateDataProcessor;
import net.orpiske.mdp.plot.RatePlotter;
import net.orpiske.mdp.plot.RateReader;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReportGenerator extends DirectoryWalker {
    private static Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private List<ReportFile> files = new LinkedList<>();

    private void plotHdr(File file)  {
        try {
            // HDR Converter
            HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper();

            String csvFile = processorWrapper.convertLog(file.getPath());

            // CSV Reader
            HdrReader reader = new HdrReader();

            HdrData hdrData = reader.read(csvFile);

            // HdrPlotter
            HdrPlotter plotter = new HdrPlotter(FilenameUtils.removeExtension(file.getPath()));

            plotter.setOutputWidth(1024);
            plotter.setOutputHeight(600);
            plotter.plot(hdrData.getPercentile(), hdrData.getValue());

            files.add(new HdrHistogramReportFile(file));
        }
        catch (Throwable t) {
            logger.error("Unable to generate report for {}", file.getPath());
            logger.error("Exception: ", t);

            ReportFile reportFile = new HdrHistogramReportFile(file);
            reportFile.setReportSuccessful(false);
            reportFile.setReportFailure(t);
        }
    }

    private void plotRate(File file) {
        try {
            RateDataProcessor rateDataProcessor = new RateDataProcessor();
            RateReader rateReader = new RateReader(rateDataProcessor);

            rateReader.read(file.getPath());

            RateData rateData = rateDataProcessor.getRateData();

            // Removes the gz
            String baseName = FilenameUtils.removeExtension(file.getPath());
            // Removes the csv
            baseName = FilenameUtils.removeExtension(baseName);

            // Plotter
            RatePlotter plotter = new RatePlotter(FilenameUtils.removeExtension(baseName));
            logger.debug("Number of records to plot: {} ", rateData.getRatePeriods().size());
            for (Date d : rateData.getRatePeriods()) {
                logger.debug("Adding date record for plotting: {}", d);
            }

            plotter.setOutputWidth(1024);
            plotter.setOutputHeight(600);
            plotter.plot(rateData.getRatePeriods(), rateData.getRateValues());
            files.add(new MptReportFile(file));
        }
        catch (Throwable t) {
            logger.error("Unable to generate report for {}", file.getPath());
            logger.error("Exception: ", t);

            ReportFile reportFile = new MptReportFile(file);
            reportFile.setReportSuccessful(false);
            reportFile.setReportFailure(t);
        }
    }

    @Override
    protected void handleFile(File file, int depth, Collection results)
            throws IOException

    {
        logger.debug("Processing file {}", file.getPath());
        String ext = FilenameUtils.getExtension(file.getName());

        if (("hdr").equals(ext)) {
            plotHdr(file);
        }

        if (("gz").equals(ext)) {
            if (!file.getName().contains("inspector")) {
                plotRate(file);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public List<ReportFile> generate(final File reportsDir) {

        if (logger.isDebugEnabled()) {
            logger.debug("Processing downloaded reports on {}", reportsDir.getName());
        }

        try {
           if (reportsDir.exists()) {
                walk(reportsDir, new ArrayList());
            }
            else {
                logger.error("The reports directory does not exist: {}", reportsDir.getPath());
            }
        } catch (IOException e) {
            logger.error("Unable to walk the whole directory: " + e.getMessage(), e);
            logger.error("Returning a partial list of all the reports due to errors");
        }

        return files;
    }


    public static void generate(String path) {
        ReportGenerator walker = new ReportGenerator();

        List<ReportFile> tmpList = walker.generate(new File(path));

        Map<String, Object> context = ReportContextBuilder.toContext(tmpList);

        // Generate the host report
        Set<ReportDirInfo> reports = (Set<ReportDirInfo>) context.get("reportDirs");

        for (ReportDirInfo report : reports) {
            logger.info("Processing report dir: {}", report);
            Map<String, Object> nodeReportContext = NodeContextBuilder.toContext(report);
            NodeReportRenderer reportRenderer = new NodeReportRenderer(nodeReportContext);

            try {
                File outFile = new File(report.getReportDir(), "index.html");
                FileUtils.writeStringToFile(outFile, reportRenderer.renderNodeInfo(), Charsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
