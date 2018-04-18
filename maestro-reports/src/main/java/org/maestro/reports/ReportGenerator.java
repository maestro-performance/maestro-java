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


import org.apache.commons.io.FileUtils;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.reports.files.ReportDirInfo;
import org.maestro.reports.files.ReportFile;
import org.maestro.reports.index.IndexRenderer;
import org.maestro.reports.node.NodeContextBuilder;
import org.maestro.reports.node.NodeReportRenderer;
import org.maestro.reports.plotter.PlotterWrapper;
import org.maestro.reports.plotter.PlotterWrapperRegistry;
import org.maestro.reports.processors.ReportFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    private final List<ReportFileProcessor> preProcessors = new LinkedList<>();
    private final List<ReportFileProcessor> postProcessors = new LinkedList<>();

    // Probably this is not the best way to do what I want (to be able to create different types of
    // plotter objects) but it gives the flexibility I need now.
    private PlotterWrapperRegistry registry = new PlotterWrapperRegistry();

    private final IndexRenderer indexRenderer = new IndexRenderer();
    private final NodeReportRenderer reportRenderer = new NodeReportRenderer();

    private final String path;

    public ReportGenerator(final String path) {
        this.path = path;
    }

    private void plotReportFile(final ReportFile reportFile) {
        if (logger.isDebugEnabled()) {
            logger.debug("Will report file {} on thread {}", reportFile, Thread.currentThread().getId());
        }

        logger.info("Plotting maestro data from {}", reportFile);
        PlotterWrapper plotterWrapper = registry.getWrapper(reportFile.getClass());

        try {
            plotterWrapper.plot(reportFile.getSourceFile());
        }
        catch (EmptyDataSet | IncompatibleDataSet e) {
            if (reportFile.isTestSuccessful()) {
                logger.error("File {} contains invalid data for a successful test", reportFile);
                throw e;
            }
        }
    }

    private void renderReportIndex(final File baseDir, final Map<String, Object> context) {
        File outFile = new File(path, "index.html");
        try {
            FileUtils.writeStringToFile(outFile, indexRenderer.render(context), StandardCharsets.UTF_8);
            indexRenderer.copyResources(baseDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderNodePage(final ReportDirInfo report) {
        logger.info("Processing report dir: {}", report.getReportDir());
        Map<String, Object> nodeReportContext = NodeContextBuilder.toContext(report);

        try {
            File outFile = new File(report.getReportDir(), "index.html");
            FileUtils.writeStringToFile(outFile, reportRenderer.render(nodeReportContext), StandardCharsets.UTF_8);
            reportRenderer.copyResources(outFile.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generate() {
        File baseDir = new File(path);

        // Step 1: build the file list for processing
        logger.info("Building the file list to be processed by the report generator");
        final ReportDirectoryWalker processor = new ReportDirectoryWalker(path);
        final List<ReportFile> fileList = processor.generate(baseDir);
        logger.info("There are {} files to be processed", fileList.size());

        fileList.parallelStream()
                .filter(item -> isMaestroReport(item))
                .forEach(this::plotReportFile);


        // Step 2: execute the pre-processors
        preProcessors.forEach(item -> item.process(fileList));

        // Step 3: build the report context
        Map<String, Object> context = ReportContextBuilder.toContext(fileList, baseDir);
        @SuppressWarnings("unchecked")
        Set<ReportDirInfo> reports = (Set<ReportDirInfo>) context.get("reportDirs");

        // Step 4: render the pages for each host
        reports.parallelStream().forEach(this::renderNodePage);

        // Step 5: render the index page
        renderReportIndex(baseDir, context);

        // Step 6: execute the post-processors
        postProcessors.forEach(item -> item.process(fileList));
    }

    private boolean isMaestroReport(ReportFile item) {
        String[] maestroFilesExt = { "csv.gz", "csv", "hdr"};

        for (String maestroFileExt : maestroFilesExt) {
            if (item.getSourceFile().getName().endsWith(maestroFileExt)) {
                return true;
            }
        }

        return false;
    }

    public List<ReportFileProcessor> getPreProcessors() {
        return preProcessors;
    }

    public List<ReportFileProcessor> getPostProcessors() {
        return postProcessors;
    }
}
