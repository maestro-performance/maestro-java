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
import org.maestro.client.notes.PingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PingInfo extends Collector {
    private static final Logger logger = LoggerFactory.getLogger(PingInfo.class);
    private static PingInfo instance = null;

    private final Map<String, PingResponse> records = new HashMap<>();

    private PingInfo() {}

    public synchronized static PingInfo getInstance() {
        if (instance == null) {
            instance = new PingInfo();
        }

        return instance;
    }


    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();

        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("maestro_ping",
                "Ping", Arrays.asList("peer", "type"));

        logger.trace("Number of values to process: {}", records.values().size());
        for (PingResponse pingResponse : records.values()) {

            logger.trace("Adding record for {}/{}", pingResponse.getPeerInfo().prettyName(), pingResponse.getId());
            labeledGauge.addMetric(Arrays.asList(pingResponse.getPeerInfo().peerName(), pingResponse.getPeerInfo().peerHost()),
                    pingResponse.getElapsed());
        }

        mfs.add(labeledGauge);
        records.clear();
        return mfs;
    }

    public void record(PingResponse pingResponse) {
        logger.trace("Recording ping for {}/{}", pingResponse.getPeerInfo().prettyName(), pingResponse.getId());
        records.put(pingResponse.getId(), pingResponse);
    }

}
