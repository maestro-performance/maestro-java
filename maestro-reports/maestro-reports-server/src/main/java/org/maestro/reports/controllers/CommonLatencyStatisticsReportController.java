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

package org.maestro.reports.controllers;

import io.javalin.Context;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.latency.serializer.Latency;
import org.maestro.plotter.latency.serializer.LatencyDistribution;
import org.maestro.plotter.latency.serializer.SmoothLatencySerializer;
import org.maestro.reports.controllers.common.LatencyStatisticsResponse;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

abstract class CommonLatencyStatisticsReportController extends AbstractReportFileController {
    private static final Logger logger = LoggerFactory.getLogger(CommonLatencyStatisticsReportController.class);

    protected void processReports(final Report report, final LatencyStatisticsResponse latencyStatisticsResponse) {
        final File file = getReportFile(report, "receiverd-latency.hdr");

        MaestroSerializer<?> serializer = new SmoothLatencySerializer();
        try {
            logger.info("Processing report data for {}", file);

            LatencyDistribution data = (LatencyDistribution) serializer.serialize(file);

            Map<String, Latency> values = data.getLatencyDistribution();

            Latency serviceTimeLatency = values.get("serviceTime");
            if (serviceTimeLatency != null) {
                latencyStatisticsResponse.getServiceTimeStatistics().add(serviceTimeLatency.getStatistics());
            }

            Latency responseTimeLatency = values.get("responseTime");
            if (responseTimeLatency != null) {
                latencyStatisticsResponse.getResponseTimeStatistics().add(responseTimeLatency.getStatistics());
            }
        } catch (IOException e) {
            logger.error("Unable to process data: {}", e.getMessage(), e);
        }
    }
}
