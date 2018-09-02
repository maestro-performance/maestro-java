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

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;

import java.time.Instant;
import java.util.*;

/**
 * A class represents data about router link
 */
@PropertyName(name="connections")
public class ConnectionsData implements ReportData {
    public static final String DEFAULT_FILENAME = "connections.properties";

    private final Set<ConnectionsRecord> recordSet = new TreeSet<>();

    public void add(ConnectionsRecord record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public Set<ConnectionsRecord> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    public ConnectionsRecord getAt(final Instant instant) {
        return recordSet.stream().findFirst().filter(record -> record.getTimestamp().equals(instant)).orElse(null);
    }

    /**
     * Number of records
     * @return the number of records/samples
     */
    public int getNumberOfSamples() {
        return recordSet.size();
    }
}
