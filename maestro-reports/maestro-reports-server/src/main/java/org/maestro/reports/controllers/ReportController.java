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

package org.maestro.reports.controllers;

import io.javalin.Context;
import io.javalin.Handler;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.reports.common.serializer.registry.FileSerializerRegistry;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportController implements Handler {
    public class ReportInfo {
        String role;
        Map<String, Object> reportData = new LinkedHashMap<>();

        public Map<String, Object> getReportData() {
            return reportData;
        }

        public void add(final String name, List<Object> reportData) {
            this.reportData.put(name, reportData);
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private static final FileSerializerRegistry registry = FileSerializerRegistry.getInstance();

    private final ReportDao reportDao = new ReportDao();

    private void processReports(final Report report, final ReportInfo reportInfo) {
        final File reportDir = new File(report.getLocation());

        File[] files = reportDir.listFiles();
        if (files == null) {
            logger.error("The report directory {} does not contain report files", reportDir);

            throw new MaestroException("The report directory does not contain report files");
        }

        for (File file : files) {
            MaestroSerializer<?> serializer = registry.getSerializer(file.getName());

            if (serializer != null) {
                try {
                    logger.info("Processing report data for {}", file);
                    reportInfo.reportData.put(serializer.name(), serializer.serialize(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            String ext = FilenameUtils.getExtension(file.getName());
//
//            if (Constants.FILE_EXTENSION_HDR_HISTOGRAM.equals(ext)) {
////                processHistogramReport(file, results);
//
//                return;
//            }
//
//            if (Constants.FILE_EXTENSION_MAESTRO.equals(ext)) {
//                if (!report.getTestHostRole().contains(Constants.FILE_HINT_INSPECTOR)) {
//                    // processMaestroReport(file, results);
//
//                    return;
//                }
//            }
//
//
//            if (Constants.FILE_EXTENSION_INSPECTOR_REPORT.equals(ext)) {
//                if (file.getPath().contains(Constants.FILE_HINT_INSPECTOR)) {
////                    processInspectorFile(file, results);
//
//                    return;
//                }
//            }

        }

    }


    @Override
    public void handle(Context context) throws Exception {
        try {
            int id = Integer.parseInt(context.param("id"));

            Report report = reportDao.fetch(id);

            ReportInfo reportInfo = new ReportInfo();

            reportInfo.setRole(report.getTestHostRole());
            processReports(report, reportInfo);
            context.json(reportInfo);
        }
        catch (Throwable t) {
            context.status(500);
            context.result(String.format("Internal server error: %s", t.getMessage()));
        }


    }
}
