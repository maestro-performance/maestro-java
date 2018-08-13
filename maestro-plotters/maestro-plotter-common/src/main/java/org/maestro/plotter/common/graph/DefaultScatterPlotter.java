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

package org.maestro.plotter.common.graph;

import org.apache.commons.compress.utils.Lists;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * A default scatter plotter
 * @param <T> report data type
 */
public abstract class DefaultScatterPlotter<T> extends AbstractPlotter<T> {
    protected static final String DEFAULT_FILENAME = "";

    protected XYChart createChart() {

        XYChart ret = baseChart();

        ret.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        ret.getStyler().setMarkerSize(16);
        return ret;
    }

    protected void createChart(String yTitle, final File outputFile, Map<Date, Statistics> stats) {
        final List<Date> periods = Lists.newArrayList(stats.keySet().iterator());

        final List<List<Double>> data = getPlotData(periods, stats);

        updateChart("", "",  "", yTitle);

        // Create Chart
        XYChart chart = createChart();

        // Series
        chart.addSeries("Mean", periods, data.get(0));

        chart.addSeries("Max", periods, data.get(1));

        chart.addSeries("Min", periods, data.get(2));


        encode(chart, outputFile);
    }

    private List<List<Double>> getPlotData(List<Date> periods, Map<Date, Statistics> stats) {
        final List<Double> means = new ArrayList<>();
        stats.values().forEach(value -> means.add(value.getMean()));


        validateDataSet(periods, means);

        final List<Double> max = new ArrayList<>();
        stats.values().forEach(value -> max.add(value.getMax()));

        validateDataSet(periods, max);

        final List<Double> min = new ArrayList<>();
        stats.values().forEach(value -> min.add(value.getMin()));

        validateDataSet(periods, min);

        return new ArrayList<List<Double>>() {{
            add(means);
            add(max);
            add(min);
        }};
    }
}
