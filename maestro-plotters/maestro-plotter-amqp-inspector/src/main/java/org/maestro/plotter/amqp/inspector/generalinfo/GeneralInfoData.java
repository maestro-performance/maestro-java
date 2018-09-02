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

package org.maestro.plotter.amqp.inspector.generalinfo;

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
public class GeneralInfoData implements ReportData {
    public static final String DEFAULT_FILENAME = "general.properties";

    private final Set<GeneralInfoRecord> recordSet = new TreeSet<>();
    private Statistics addressesStatistics = null;
    private Statistics connectionsStatistics = null;
    private Statistics linkRoutesStatistics = null;
    private Statistics autoLinksStatistics = null;

    public void add(GeneralInfoRecord record) {
        recordSet.add(record);
    }

    public List<Date> getPeriods() {
        List<Date> list = new ArrayList<>(recordSet.size());

        recordSet.forEach(item->list.add(Date.from(item.getTimestamp())));

        return list;
    }

    public Set<GeneralInfoRecord> getRecordSet() {
        return new TreeSet<>(recordSet);
    }

    public GeneralInfoRecord getAt(final Instant instant) {
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
    protected Statistics addressesStatistics() {
        if (addressesStatistics == null) {
            addressesStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(GeneralInfoRecord::getAddressCount));
        }

        return addressesStatistics;
    }

    @PropertyProvider(name="-AddressCount")
    public double getAddressesCount() {
        return addressesStatistics().getMax();
    }

    /**
     * Get the statistics for the undelivered messages
     * @return A Statistics object for the undelivered messages
     */
    protected Statistics connectionsStatistics() {
        if (connectionsStatistics == null) {
            connectionsStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(GeneralInfoRecord::getConnetionsCount));
        }

        return connectionsStatistics;
    }

    @PropertyProvider(name="-ConnectionsCount")
    public double getConnectionsCount() {
        return connectionsStatistics().getMax();
    }

    /**
     * Get the statistics for the accepted messages
     * @return A Statistics object for the accepted messages
     */
    protected Statistics linkRoutesStatistics() {
        if (linkRoutesStatistics == null) {
            linkRoutesStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(GeneralInfoRecord::getLinkRoutersCount));
        }

        return linkRoutesStatistics;
    }

    @PropertyProvider(name="-LinkRoutesCount")
    public double getLinkRoutesCount() {
        return linkRoutesStatistics().getMax();
    }

    /**
     * Get the statistics for the released messages
     * @return A Statistics object for the released messages
     */
    protected Statistics autoLinksStatistics() {
        if (autoLinksStatistics == null) {
            autoLinksStatistics = StatisticsBuilder.of(recordSet.stream().mapToDouble(GeneralInfoRecord::getAutoLinksCount));
        }

        return autoLinksStatistics;
    }

    @PropertyProvider(name="-AutoLinksCount")
    public double getAutoLinksCount() {
        return autoLinksStatistics().getMax();
    }

}
