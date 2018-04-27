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
