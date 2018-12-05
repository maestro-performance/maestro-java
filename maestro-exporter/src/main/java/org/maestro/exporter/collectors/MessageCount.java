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

public class MessageCount extends Collector {
    private static final Logger logger = LoggerFactory.getLogger(MessageCount.class);
    private static MessageCount instance = null;

    private final Map<String, StatsResponse> records = new HashMap<>();

    private MessageCount() { }

    public synchronized static MessageCount getInstance() {
        if (instance == null) {
            instance = new MessageCount();
        }

        return instance;
    }

    public List<Collector.MetricFamilySamples> collect() {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();

        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("maestro_message_count",
                "Message count", Arrays.asList("peer", "type"));

        logger.trace("Number of values to process: {}", records.values().size());

        for (StatsResponse stats : records.values()) {
            logger.trace("Adding record for {}/{}", stats.getPeerInfo().prettyName(), stats.getId());
            labeledGauge.addMetric(Arrays.asList(stats.getPeerInfo().peerName(), stats.getPeerInfo().peerName()), stats.getCount());
        }

        mfs.add(labeledGauge);
        records.clear();
        return mfs;
    }

    public void record(StatsResponse stats) {
        logger.trace("Recording message count for {}/{}", stats.getPeerInfo().prettyName(), stats.getId());
        records.put(stats.getId(), stats);
    }
}
