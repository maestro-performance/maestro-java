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

import java.util.*;

public class LatencyResponse<T> implements Response {
    @JsonProperty("Percentiles")
    Set<String> categories = new TreeSet<>();

    @JsonProperty("ServiceTime")
    private List<Double> serviceTime = new ArrayList<>();

    @JsonProperty("ResponseTime")
    private List<Double> responseTime = new ArrayList<>();

    public Set<String> getCategories() {
        return categories;
    }

    public List<Double> getServiceTime() {
        return serviceTime;
    }

    public List<Double> getResponseTime() {
        return responseTime;
    }

    public void setServiceTime(List<Double> serviceTime) {
        this.serviceTime = serviceTime;
    }

    public void setResponseTime(List<Double> responseTime) {
        this.responseTime = responseTime;
    }

    public static String categoryName(int testId, int testNumber, String name) {
        return String.format("%d/%d %s", testId, testNumber, name);
    }
}