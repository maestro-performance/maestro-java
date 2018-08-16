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

package org.maestro.plotter.rate.graph;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.graph.DefaultHistogramPlotter;
import org.maestro.plotter.rate.RateData;

import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.List;

public class RatePlotter extends DefaultHistogramPlotter<RateData> {

    @Override
    public void plot(RateData reportData, File outputFile) throws MaestroException {
        updateChart("", "",  "", "Messages p/ Second");

        // Create Chart
        XYChart chart = createChart();

        List<Long> rateValues = reportData.getRateValues();
        List<Date> periods = reportData.getPeriods();
        validateDataSet(periods, rateValues);

        // Series
        XYSeries used = chart.addSeries("Rate", periods, rateValues);

        used.setLineColor(XChartSeriesColors.BLUE);
        used.setMarkerColor(Color.LIGHT_GRAY);
        used.setMarker(SeriesMarkers.NONE);
        used.setLineStyle(SeriesLines.SOLID);

        encode(chart, outputFile);
    }
}
