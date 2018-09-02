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

package org.maestro.plotter.amqp.inspector.routerlink;

import org.maestro.plotter.common.ReportData;
import org.maestro.plotter.common.properties.annotations.PropertyName;
import org.maestro.plotter.common.properties.annotations.PropertyProvider;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.common.statistics.StatisticsBuilder;

import java.time.Instant;
import java.util.*;

/**
 * A class represents data about router link
 */
@PropertyName(name="routerLink")
public class RouterLinkData implements ReportData {
    public static final String DEFAULT_FILENAME = "routerLink.properties";

    private final Set<RouterLinkRecord> recordSet = new TreeSet<>();
    private Statistics deliveredStatistics = null;
    private Statistics undeliveredStatistics = null;
    private Statistics acceptedStatistics = null;
    private Statistics releasedStatistics = null;

    public void add(RouterLinkRecord record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public Set<RouterLinkRecord> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    public RouterLinkRecord getAt(final Instant instant) {
        return recordSet.stream().findFirst().filter(record -> record.getTimestamp().equals(instant)).orElse(null);
    }

    /**
     * Number of records
     * @return the number of records/samples
     */
    public int getNumberOfSamples() {
        return recordSet.size();
    }

    /**
     * Get the statistics for the delivered messages count
     * @return A Statistics object for the delivered messages count
     */
    protected Statistics deliveredStatistics() {
        if (deliveredStatistics == null) {
            deliveredStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(RouterLinkRecord::getDeliveryCount));
        }

        return deliveredStatistics;
    }

    @PropertyProvider(name="-DeliveryCount")
    public double getDeliveredCount() {
        return deliveredStatistics().getMax();
    }

    /**
     * Get the statistics for the undelivered messages
     * @return A Statistics object for the undelivered messages
     */
    protected Statistics undeliveredStatistics() {
        if (undeliveredStatistics == null) {
            undeliveredStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(RouterLinkRecord::getUndeliveredCount));
        }

        return undeliveredStatistics;
    }

    @PropertyProvider(name="-UndeliveredCount")
    public double getUndeliveredCount() {
        return undeliveredStatistics().getMax();
    }

    /**
     * Get the statistics for the accepted messages
     * @return A Statistics object for the accepted messages
     */
    protected Statistics acceptedStatistics() {
        if (acceptedStatistics == null) {
            acceptedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(RouterLinkRecord::getAcceptedCount));
        }

        return acceptedStatistics;
    }

    @PropertyProvider(name="-AcceptedCount")
    public double getAcceptedCount() {
        return acceptedStatistics().getMax();
    }

    /**
     * Get the statistics for the released messages
     * @return A Statistics object for the released messages
     */
    protected Statistics releasedStatistics() {
        if (releasedStatistics == null) {
            releasedStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(RouterLinkRecord::getReleasedCount));
        }

        return releasedStatistics;
    }

    @PropertyProvider(name="-ReleasedCount")
    public double getReleasedCount() {
        return releasedStatistics().getMax();
    }

}
