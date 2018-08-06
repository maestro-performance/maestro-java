/*
 * Copyright 2017 Otavio Rodolfo Piske
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.plotter.latency.graph;


import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.latency.common.HdrData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.List;


/**
 * The plotter for HDR histograms
 */
@SuppressWarnings("unused")
public class HdrPlotter extends AbstractHdrPlotter<HdrPlotter.HdrDataWrapper, HdrData> {
    class HdrDataWrapper {
        private final List<Double> percentiles;
        private final List<Double> values;

        public HdrDataWrapper(final HdrData data) {
            percentiles = data.getPercentile();
            values = data. getValue();
        }


        public List<Double> getPercentiles() {
            return percentiles;
        }

        public List<Double> getValues() {
            return values;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HdrPlotter.class);

    public HdrPlotter(final String baseName) {
        super(baseName);
    }

    public HdrPlotter(final String baseName, final String timeUnit) {
        super(baseName, timeUnit);
    }

    protected void plotDataAt(final HdrPlotter.HdrDataWrapper data, final Double min, final File fileName) {
        XYChart chart = baseChart();

        chart.getStyler().setXAxisMin(min);

        // Series
        XYSeries series = chart.addSeries(getChartProperties().getSeriesName(), data.getPercentiles(), data.getValues());

        series.setLineColor(XChartSeriesColors.BLUE);
        series.setMarkerColor(Color.LIGHT_GRAY);
        series.setMarker(SeriesMarkers.NONE);
        series.setLineStyle(SeriesLines.SOLID);

        encode(chart, fileName);
    }


    public void plot(final HdrData reportData, final File outputDir) throws MaestroException {
        HdrDataWrapper dataWrapper = new HdrDataWrapper(reportData);

        validateDataSet(dataWrapper.getPercentiles(), dataWrapper.getValues());

        launchAsyncPlots(outputDir, dataWrapper);
    }


}
