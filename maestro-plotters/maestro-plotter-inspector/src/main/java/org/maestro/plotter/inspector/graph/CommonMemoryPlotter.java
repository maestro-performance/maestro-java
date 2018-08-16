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

import org.apache.commons.io.FileUtils;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.graph.DefaultHistogramPlotter;
import org.maestro.plotter.inspector.common.CommonMemoryData;

import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.List;

class CommonMemoryPlotter<T extends CommonMemoryData<?>> extends DefaultHistogramPlotter<T> {

    @Override
    public void plot(final T reportData, final File outputFile) throws MaestroException {
        updateChart("", "",  "", "Megabytes");

        // Create Chart
        XYChart chart = createChart();


        final List<Long> scaledUsed = reportData.getUsed(FileUtils.ONE_MB);
        final List<Date> periods = reportData.getPeriods();

        validateDataSet(periods, scaledUsed);

        // Series
        XYSeries used = chart.addSeries("Used", periods, scaledUsed);

        used.setLineColor(XChartSeriesColors.BLUE);
        used.setMarkerColor(Color.LIGHT_GRAY);
        used.setMarker(SeriesMarkers.NONE);
        used.setLineStyle(SeriesLines.SOLID);

        List<Long> scaledCommited = reportData.getCommitted(FileUtils.ONE_MB);

        // Series
        XYSeries committed = chart.addSeries("Committed", periods, scaledCommited);

        committed.setLineColor(XChartSeriesColors.GREEN);
        committed.setMarkerColor(Color.LIGHT_GRAY);
        committed.setMarker(SeriesMarkers.NONE);
        used.setLineStyle(SeriesLines.SOLID);


        List<Long> scaledMax = reportData.getMax(FileUtils.ONE_MB);

        // Series
        XYSeries max = chart.addSeries("Max", periods, scaledMax);

        max.setLineColor(XChartSeriesColors.RED);
        max.setMarkerColor(Color.LIGHT_GRAY);
        max.setMarker(SeriesMarkers.NONE);
        max.setLineStyle(SeriesLines.SOLID);

        encode(chart, outputFile);
    }
}
