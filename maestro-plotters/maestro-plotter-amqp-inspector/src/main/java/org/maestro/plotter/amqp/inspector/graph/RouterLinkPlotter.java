package org.maestro.plotter.amqp.inspector.graph;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkDataSet;
import org.maestro.plotter.common.graph.AbstractInterconnectPlotter;
import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * A plotter for router link data
 */
public class RouterLinkPlotter extends AbstractInterconnectPlotter<RouterLinkDataSet> {
    public static final String DEFAULT_FILENAME = "routerLink_";

    /**
     * Plotter
     * @param dataSet collected data
     * @param outputDir output file
     * @throws MaestroException implementation specific
     */
    @Override
    public void plot(final RouterLinkDataSet dataSet, final File outputDir) throws MaestroException {
        final Map<String, Map<Date, Statistics>> stats = dataSet.getStatistics();

        for (Map.Entry<String, Map<Date, Statistics>> entry : stats.entrySet())
        {
            plot(entry.getValue(), outputDir, entry.getKey());
        }
    }

    @Override
    public String getDefaultName() {
        return DEFAULT_FILENAME;
    }
}
