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

package org.maestro.plotter.amqp.inspector.connections;


import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * A class represents router link data set
 */
@PropertyName(name="qdmemory-")
public class ConnectionsDataSet {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionsDataSet.class);
    private final Map<String, ConnectionsData> map = new HashMap<>();

    /**
     * Add a record to the data set
     * @param connectionsRecord record
     */
    public void add(final ConnectionsRecord connectionsRecord) {
        ConnectionsData connectionsData = map.get(connectionsRecord.getName());

        if (connectionsData == null) {
            connectionsData = new ConnectionsData();
        }

        connectionsData.add(connectionsRecord);
        map.put(connectionsRecord.getName(), connectionsData);
    }


    /**
     * Get all records
     * @return map of records
     */
    @PropertyProvider(name="")
    public Map<String, ConnectionsData> getMap() {
        return map;
    }

//
    private void doCalc(Map<Instant, ConnectionsData> calcMap, final String key, final ConnectionsData data) {
        logger.trace("Processing record at instant for queue {}", key);

        Set<ConnectionsRecord> connectionsRecords = data.getRecordSet();

        for (ConnectionsRecord record : connectionsRecords) {
            calcMap.put(record.getTimestamp(), data);
        }
    }

    /**
     * Gets the aggregated statistics for the set on a per period basis. Do not confuse
     * with the statistics returned on a per-queue basis.
     * @return statistics
     */
    public Map<Date, ConnectionsData> getSummary() {
        Map<Instant, ConnectionsData> calcMap = new HashMap<>();
        map.forEach((key, value) -> doCalc(calcMap, key, value));

        Map<Date, ConnectionsData> ret = new TreeMap<>();
        calcMap.forEach((key, value) -> ret.put(Date.from(key), value));

        return ret;
    }
}
