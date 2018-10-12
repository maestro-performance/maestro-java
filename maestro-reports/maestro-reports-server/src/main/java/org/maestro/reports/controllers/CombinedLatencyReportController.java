/*
 * Copyright 2018 Otavio Rodolfo Piske
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
import org.maestro.common.HostTypes;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.latency.serializer.Latency;
import org.maestro.plotter.latency.serializer.LatencyDistribution;
import org.maestro.plotter.latency.serializer.SmoothLatencySerializer;
import org.maestro.reports.controllers.common.LatencyResponse;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedLatencyReportController extends AbstractReportFileController{
    private static final Logger logger = LoggerFactory.getLogger(CombinedLatencyReportController.class);

    private final ReportDao reportDao = new ReportDao();

    private void processReports(final Report report, final LatencyResponse latencyDistribution) {
        File file = getReportFile(report, "receiverd-latency.hdr");

        MaestroSerializer<?> serializer = new SmoothLatencySerializer();
        try {
            logger.info("Processing report data for {}", file);

            LatencyDistribution data = (LatencyDistribution) serializer.serialize(file);

            Map<String, Latency> values = data.getLatencyDistribution();

            Latency serviceTimeLatency = values.get("serviceTime");

            if (serviceTimeLatency != null) {
                if (latencyDistribution.getCategories().isEmpty()) {
                    List<String> categories = serviceTimeLatency.getPercentiles()
                            .stream().map(String::valueOf).collect(Collectors.toList());
                    latencyDistribution.getCategories().addAll(categories);
                }

                latencyDistribution.setServiceTime(serviceTimeLatency.getValues());
            }

            Latency responseTimeLatency = values.get("responseTime");
            if (responseTimeLatency != null) {
                latencyDistribution.setResponseTime(responseTimeLatency.getValues());
            }
        } catch (IOException e) {
            logger.error("Unable to process data: {}", e.getMessage(), e);
        }
    }


    @Override
    public void handle(Context context) {
        try {
            int id = Integer.parseInt(context.param("id"));

            List<Report> reports = reportDao.fetchByTest(id, HostTypes.RECEIVER_HOST_TYPE);

            LatencyResponse response = new LatencyResponse();

            reports.forEach(r -> processReports(r, response));

            context.json(response);
        }
        catch (Throwable t) {
            context.status(500);
            context.result(String.format("Internal server error: %s", t.getMessage()));
        }
    }
}
