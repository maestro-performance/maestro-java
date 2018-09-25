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

package org.maestro.plotter.latency.serializer;

public class Statistics {
    private long latencyStartTS;
    private long latencyEndTS;
    private double latencyMaxValue;
    private long latency50th;
    private long latency90th;
    private long latency95th;
    private long latency99th;
    private long latency999th;
    private long latency9999th;
    private long latency99999th;
    private double latencyStdDeviation;
    private long latencyTotalCount;
    private double latencyMean;

    public long getLatencyStartTS() {
        return latencyStartTS;
    }

    public void setLatencyStartTS(long latencyStartTS) {
        this.latencyStartTS = latencyStartTS;
    }

    public long getLatencyEndTS() {
        return latencyEndTS;
    }

    public void setLatencyEndTS(long latencyEndTS) {
        this.latencyEndTS = latencyEndTS;
    }

    public double getLatencyMaxValue() {
        return latencyMaxValue;
    }

    public void setLatencyMaxValue(double latencyMaxValue) {
        this.latencyMaxValue = latencyMaxValue;
    }

    public long getLatency50th() {
        return latency50th;
    }

    public void setLatency50th(long latency50th) {
        this.latency50th = latency50th;
    }

    public long getLatency90th() {
        return latency90th;
    }

    public void setLatency90th(long latency90th) {
        this.latency90th = latency90th;
    }

    public long getLatency95th() {
        return latency95th;
    }

    public void setLatency95th(long latency95th) {
        this.latency95th = latency95th;
    }

    public long getLatency99th() {
        return latency99th;
    }

    public void setLatency99th(long latency99th) {
        this.latency99th = latency99th;
    }

    public long getLatency999th() {
        return latency999th;
    }

    public void setLatency999th(long latency999th) {
        this.latency999th = latency999th;
    }

    public long getLatency9999th() {
        return latency9999th;
    }

    public void setLatency9999th(long latency9999th) {
        this.latency9999th = latency9999th;
    }

    public long getLatency99999th() {
        return latency99999th;
    }

    public void setLatency99999th(long latency99999th) {
        this.latency99999th = latency99999th;
    }

    public double getLatencyStdDeviation() {
        return latencyStdDeviation;
    }

    public void setLatencyStdDeviation(double latencyStdDeviation) {
        this.latencyStdDeviation = latencyStdDeviation;
    }

    public long getLatencyTotalCount() {
        return latencyTotalCount;
    }

    public void setLatencyTotalCount(long latencyTotalCount) {
        this.latencyTotalCount = latencyTotalCount;
    }

    public double getLatencyMean() {
        return latencyMean;
    }

    public void setLatencyMean(double latencyMean) {
        this.latencyMean = latencyMean;
    }
}
