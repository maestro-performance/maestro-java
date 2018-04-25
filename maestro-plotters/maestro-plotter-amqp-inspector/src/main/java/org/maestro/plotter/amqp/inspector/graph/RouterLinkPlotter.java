package org.maestro.plotter.amqp.inspector.graph;

import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkDataSet;
import org.maestro.plotter.common.graph.DefaultScatterPlotter;
import org.maestro.plotter.common.statistics.Statistics;

import org.apache.commons.compress.utils.Lists;
import org.knowm.xchart.XYChart;
import org.maestro.common.exceptions.MaestroException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RouterLinkPlotter extends DefaultScatterPlotter<RouterLinkDataSet> {
    public static final String DEFAULT_FILENAME = "routerLink.png";

    @Override
    public void plot(final RouterLinkDataSet dataSet, final File outputFile) throws MaestroException {
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
