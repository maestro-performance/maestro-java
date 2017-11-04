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

package net.orpiske.mpt.exporter.collectors;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;
import net.orpiske.mpt.maestro.notes.PingResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PingInfo extends Collector {
    private String type;
    private PingResponse ping;

    public PingInfo(final String type) {
        this.type = type;
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        if (ping != null) {
            GaugeMetricFamily labeledGauge = new GaugeMetricFamily("maestro_ping",
                    "Ping", Arrays.asList("peer", "type"));

            labeledGauge.addMetric(Arrays.asList(ping.getName(), type), ping.getElapsed());

            mfs.add(labeledGauge);
        }

        return mfs;
    }

    public void eval(PingResponse stats) {
        this.ping = stats;
    }
}
