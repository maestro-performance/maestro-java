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

package org.maestro.exporter.collectors;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.maestro.client.notes.StatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConnectionCount extends Collector {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionCount.class);
    private static ConnectionCount instance = null;

    private final Map<String, StatsResponse> records = new HashMap<>();

    private ConnectionCount() { }

    public synchronized static ConnectionCount getInstance() {
        if (instance == null) {
            instance = new ConnectionCount();
        }

        return instance;
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();

        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("maestro_connection_count",
                "Connection count", Arrays.asList("peer", "type"));

        logger.trace("Number of values to process: {}", records.values().size());

        for (StatsResponse stats : records.values()) {
            labeledGauge.addMetric(Arrays.asList(stats.getPeerInfo().peerName(), stats.getPeerInfo().peerHost()), stats.getChildCount());

        }

        mfs.add(labeledGauge);
        records.clear();
        return mfs;
    }

    public void record(StatsResponse stats) {
        logger.trace("Recording connection count for {}/{}", stats.getPeerInfo().prettyName(), stats.getId());
        records.put(stats.getId(), stats);
    }
}
