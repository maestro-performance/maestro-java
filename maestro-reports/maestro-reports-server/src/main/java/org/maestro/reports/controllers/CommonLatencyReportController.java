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

import org.maestro.plotter.latency.serializer.Latency;
import org.maestro.plotter.latency.serializer.LatencyDistribution;
import org.maestro.reports.controllers.common.LatencyResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class CommonLatencyReportController extends CommonCachedLatencyReportController<LatencyResponse> {
    protected void setResponseData(LatencyResponse latencyDistribution, LatencyDistribution data) {
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
    }
}
