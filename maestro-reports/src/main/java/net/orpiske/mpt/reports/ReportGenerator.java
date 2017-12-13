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

import net.orpiske.mpt.reports.files.BmicReportFile;
import net.orpiske.mpt.reports.files.MptReportFile;
import net.orpiske.mpt.reports.files.ReportFile;
import net.orpiske.mpt.reports.index.IndexRenderer;
import net.orpiske.mpt.reports.node.NodeContextBuilder;
import net.orpiske.mpt.reports.node.NodeReportRenderer;
import net.orpiske.mpt.reports.plotter.BmicPlotter;
import net.orpiske.mpt.reports.plotter.HdrPlotter;
import net.orpiske.mpt.reports.plotter.RatePlotter;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    private String path;

    public ReportGenerator(final String path) {
        this.path = path;
    }

    private void plotMptReportFile(final ReportFile reportFile) {
        logger.debug("Will report file {} on thread {}", reportFile, Thread.currentThread().getId());

        try {
            if (reportFile instanceof MptReportFile) {
                RatePlotter plotter = new RatePlotter();

                plotter.plot(reportFile.getSourceFile());
            } else {
                if (reportFile instanceof BmicReportFile) {
                    BmicPlotter plotter = new BmicPlotter();

                    plotter.plot(reportFile.getSourceFile());
                }
                else {
                    throw new Exception("Invalid report file for: " + reportFile.getSourceFile());
                }
            }
        }
        catch (Throwable t) {
            logger.error("Unable to plot file {}: {}", reportFile.getSourceFile(), t.getMessage(), t);
        }
    }

    private void plotLatencyReportFile(final ReportFile reportFile) {
        logger.debug("Will report file {} on thread {}", reportFile, Thread.currentThread().getId());

        try {
            if (reportFile instanceof HdrHistogramReportFile) {
                logger.info("Plotting latency from {}", reportFile);

                HdrPlotter plotter = new HdrPlotter();

                plotter.plot(reportFile.getSourceFile());
            } else {
                throw new Exception("Invalid report file for: " + reportFile.getSourceFile());
            }
        }
        catch (Throwable t) {
            logger.error("Unable to plot file {}: {}", reportFile.getSourceFile(), t.getMessage(), t);
        }
    }

    private void renderReportIndex(final File baseDir, final Map<String, Object> context) {
        IndexRenderer indexRenderer = new IndexRenderer(context);
        File outFile = new File(path, "index.html");
        try {
            FileUtils.writeStringToFile(outFile, indexRenderer.render(), Charsets.UTF_8);
            indexRenderer.copyResources(baseDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderNodePage(final File baseDir, final ReportDirInfo report) {
        logger.info("Processing report dir: {}", report.getReportDir());
        Map<String, Object> nodeReportContext = NodeContextBuilder.toContext(report, baseDir);
        NodeReportRenderer reportRenderer = new NodeReportRenderer(nodeReportContext);

        try {
            String outDir = path + report.getReportDir();
            File outFile = new File(outDir, "index.html");
            FileUtils.writeStringToFile(outFile, reportRenderer.render(), Charsets.UTF_8);
            reportRenderer.copyResources(outFile.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generate() {
        File baseDir = new File(path);

        // Step 1: build the file list for processing
        logger.info("Building the file list to be processed by the report generator");
        final ReportDirProcessor processor = new ReportDirProcessor(path);
        final List<ReportFile> fileList = processor.generate(baseDir);
        logger.info("There are {} files to be processed", fileList.size());

        fileList.parallelStream()
                .filter(item -> item.getSourceFile().getName().endsWith("csv.gz"))
                .forEach(this::plotMptReportFile);

        // NOTE: this one is not thread-safe. This may have something to do
        // with capturing the console and so on.
        // TODO: find a better way to handle this
        fileList.stream()
                .filter(item -> item.getSourceFile().getName().endsWith("hdr"))
                .forEach(this::plotLatencyReportFile);


        // Step 2: build the report context
        Map<String, Object> context = ReportContextBuilder.toContext(fileList, baseDir);
        Set<ReportDirInfo> reports = (Set<ReportDirInfo>) context.get("reportDirs");

        // Step 3: render the pages for each host
        reports.parallelStream().forEach(item -> renderNodePage(baseDir, item));

        // Step 4: render the index page
        renderReportIndex(baseDir, context);

        // Step 5: clean the disk
        ReportDirPostProcessor postProcessor = new ReportDirPostProcessor(path);
        postProcessor.postProcess(baseDir);
    }

    @Deprecated
    public static void generate(final String path) {
        ReportGenerator reportGenerator = new ReportGenerator(path);

        reportGenerator.generate();
    }


}
