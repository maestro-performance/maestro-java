package org.maestro.plotter.amqp.inspector.connections;

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.statistics.Statistics;

import java.time.Instant;
import java.util.*;

/**
 * A class represents data about router link
 */
@PropertyName(name="connections")
public class ConnectionsData implements ReportData {
    public static final String DEFAULT_FILENAME = "connections.properties";

    private Set<ConnectionsRecord> recordSet = new TreeSet<>();
    private Statistics properties = null;

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
     * @return
     */
    public int getNumberOfSamples() {
        return recordSet.size();
    }
}
