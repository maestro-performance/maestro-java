package org.maestro.plotter.amqp.inspector.graph;

import org.apache.commons.compress.utils.Lists;
import org.knowm.xchart.XYChart;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.amqp.inspector.generalinfo.GeneralInfoDataSet;
import org.maestro.plotter.common.graph.DefaultScatterPlotter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A plotter for router link data
 */
public class GeneralInfoPlotter extends DefaultScatterPlotter<GeneralInfoDataSet> {
    public static final String DEFAULT_FILENAME = "general.png";

    /**
     * Plotter
     * @param dataSet collected data
     * @param outputDir output file
     * @throws MaestroException implementation specific
     */
    @Override
    public void plot(final GeneralInfoDataSet dataSet, final File outputDir) throws MaestroException {
        final Map<String, Map<Date, Statistics>> stats = dataSet.getStatistics();

        for (Map.Entry<String, Map<Date, Statistics>> entry : stats.entrySet())
        {
            plot(entry.getValue(), outputDir, entry.getKey());
        }
    }

    private void plot(final Map<Date, Statistics> reportData, final File outputDir, final String name) {
        final File outputFile = new File(outputDir, friendlyName(name));

        createChart(name, outputFile, reportData);
    }

    private static String friendlyName(final String areaName) {
        return "general_" + areaName.replace(" ", "_").toLowerCase() + ".png";
    }


    private void createChart(String yTitle, final File outputFile, Map<Date, Statistics> stats) {
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
