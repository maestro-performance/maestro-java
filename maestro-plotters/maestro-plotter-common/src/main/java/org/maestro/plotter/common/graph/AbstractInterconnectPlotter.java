package org.maestro.plotter.common.graph;

import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.Date;
import java.util.Map;


public abstract class AbstractInterconnectPlotter<T> extends DefaultScatterPlotter<T> {

    public abstract String getDefaultName ();

    private String friendlyName(final String areaName) {
        return getDefaultName() + areaName.replace(" ", "_").toLowerCase() + ".png";
    }

    protected void plot(final Map<Date, Statistics> reportData, final File outputDir, final String name) {
        final File outputFile = new File(outputDir, friendlyName(name));

        createChart(name, outputFile, reportData);
    }
}
