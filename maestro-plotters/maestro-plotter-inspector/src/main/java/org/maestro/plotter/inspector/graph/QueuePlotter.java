/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.plotter.inspector.graph;

import org.apache.commons.compress.utils.Lists;
import org.knowm.xchart.XYChart;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.graph.DefaultScatterPlotter;
import org.maestro.plotter.common.statistics.Statistics;
import org.maestro.plotter.inspector.queues.QueueDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class QueuePlotter extends DefaultScatterPlotter<QueueDataSet> {
    public static final String DEFAULT_FILENAME = "queues.png";

    @Override
    public void plot(final QueueDataSet dataSet, final File outputFile) throws MaestroException {
        final Map<Date, Statistics> stats = dataSet.getStatistics();

        final List<Date> periods = Lists.newArrayList(stats.keySet().iterator());

        final List<Double> means = new ArrayList<>();
        stats.values().forEach(value -> means.add(value.getMean()));

        validateDataSet(periods, means);

        final List<Double> max = new ArrayList<>();
        stats.values().forEach(value -> max.add(value.getMax()));

        validateDataSet(periods, max);

        final List<Double> min = new ArrayList<>();
        stats.values().forEach(value -> min.add(value.getMin()));

        validateDataSet(periods, min);

        updateChart("", "",  "", "Messages");

        // Create Chart
        XYChart chart = createChart();

        // Series
        chart.addSeries("Mean", periods, means);

        chart.addSeries("Max", periods, max);

        chart.addSeries("Min", periods, min);

        encode(chart, outputFile);
    }
}
