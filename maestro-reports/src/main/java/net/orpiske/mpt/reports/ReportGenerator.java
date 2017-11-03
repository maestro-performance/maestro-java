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

import net.orpiske.mpt.reports.index.IndexRenderer;
import net.orpiske.mpt.reports.node.NodeContextBuilder;
import net.orpiske.mpt.reports.node.NodeReportRenderer;
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

    public static void generate(String path) {
        File baseDir = new File(path);

        ReportDirProcessor processor = new ReportDirProcessor(path);
        List<ReportFile> tmpList = processor.generate(baseDir);

        Map<String, Object> context = ReportContextBuilder.toContext(tmpList, baseDir);

        // Generate the host report
        Set<ReportDirInfo> reports = (Set<ReportDirInfo>) context.get("reportDirs");

        for (ReportDirInfo report : reports) {
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

        IndexRenderer indexRenderer = new IndexRenderer(context);
        File outFile = new File(path, "index.html");
        try {
            FileUtils.writeStringToFile(outFile, indexRenderer.render(), Charsets.UTF_8);
            indexRenderer.copyResources(baseDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ReportDirPostProcessor postProcessor = new ReportDirPostProcessor(path);
        postProcessor.postProcess(baseDir);
    }
}
