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

package org.maestro.reports.controllers.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.maestro.plotter.latency.serializer.Statistics;

import java.util.LinkedList;
import java.util.List;

public class LatencyStatisticsResponse implements Response {
    @JsonProperty("ServiceTimeStatistics")
    private List<Statistics> serviceTimeStatistics = new LinkedList<>();

    @JsonProperty("ResponseTimeStatistics")
    private List<Statistics> responseTimeStatistics = new LinkedList<>();

    public List<Statistics> getServiceTimeStatistics() {
        return serviceTimeStatistics;
    }

    public List<Statistics> getResponseTimeStatistics() {
        return responseTimeStatistics;
    }

    public void setServiceTimeStatistics(List<Statistics> serviceTimeStatistics) {
        this.serviceTimeStatistics = serviceTimeStatistics;
    }

    public void setResponseTimeStatistics(List<Statistics> responseTimeStatistics) {
        this.responseTimeStatistics = responseTimeStatistics;
    }

    public static String categoryName(int testId, int testNumber, String name) {
        return String.format("%d/%d %s", testId, testNumber, name);
    }
}