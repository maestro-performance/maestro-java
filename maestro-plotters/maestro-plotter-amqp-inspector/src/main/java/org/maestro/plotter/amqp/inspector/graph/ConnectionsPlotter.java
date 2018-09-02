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

package org.maestro.plotter.amqp.inspector.graph;

import org.apache.commons.compress.utils.Lists;
import org.knowm.xchart.XYChart;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.amqp.inspector.connections.ConnectionsData;
import org.maestro.plotter.amqp.inspector.connections.ConnectionsDataSet;
import org.maestro.plotter.common.graph.DefaultScatterPlotter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A plotter for router link data
 */
public class ConnectionsPlotter extends DefaultScatterPlotter<ConnectionsDataSet> {
    public static final String DEFAULT_FILENAME = "connections.png";

    /**
     * Plotter
     * @param dataSet collected data
     * @param outputFile output file
     * @throws MaestroException implementation specific
     */
    @Override
    public void plot(final ConnectionsDataSet dataSet, final File outputFile) throws MaestroException {
        final Map<Date, ConnectionsData> stats = dataSet.getSummary();

        final List<Date> periods = Lists.newArrayList(stats.keySet().iterator());

        final List<Integer> records = new ArrayList<>();
        stats.values().forEach(value -> records.add(value.getNumberOfSamples()));


        validateDataSet(periods, records);

        updateChart("", "",  "", "Messages");

        // Create Chart
        XYChart chart = createChart();

        // Series
        chart.addSeries("Mean", periods, records);


        encode(chart, outputFile);
    }
}
