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

package org.maestro.plotter.latency.graph;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.latency.common.HdrDataCO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.List;

public class HdrPlotterWithCo extends AbstractHdrPlotter<HdrPlotterWithCo.HdrDataCoWrapper, HdrDataCO> {
    class HdrDataCoWrapper {
        private final List<Double> uncorrectedPercentiles;
        private final List<Double> uncorrectedValues;

        private final List<Double> correctedPercentiles;
        private final List<Double> correctedValues;

        public HdrDataCoWrapper(final HdrDataCO data) {
            uncorrectedPercentiles = data.getPercentile();
            uncorrectedValues = data.getValue();

            correctedPercentiles = data.getCorrected().getPercentile();
            correctedValues = data.getCorrected().getValue();
        }

        public List<Double> getUncorrectedPercentiles() {
            return uncorrectedPercentiles;
        }

        public List<Double> getUncorrectedValues() {
            return uncorrectedValues;
        }

        public List<Double> getCorrectedPercentiles() {
            return correctedPercentiles;
        }

        public List<Double> getCorrectedValues() {
            return correctedValues;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HdrPlotterWithCo.class);

    public HdrPlotterWithCo(final String baseName) {
        super(baseName);
    }

    public HdrPlotterWithCo(final String baseName, final String timeUnit) {
        super(baseName, timeUnit);
    }

    protected void plotDataAt(final HdrDataCoWrapper data, final Double min, final File fileName) {
        // Create Chart
        XYChart chart = baseChart();

        chart.getStyler().setXAxisMin(min);

        // Series
        XYSeries serviceTime = chart.addSeries("Uncorrected " + getChartProperties().getSeriesName().toLowerCase(),
                data.getUncorrectedPercentiles(), data.getUncorrectedValues());

        serviceTime.setLineColor(XChartSeriesColors.RED);
        serviceTime.setMarkerColor(Color.LIGHT_GRAY);
        serviceTime.setMarker(SeriesMarkers.NONE);
        serviceTime.setLineStyle(SeriesLines.SOLID);

        // Series
        XYSeries responseTime = chart.addSeries("Corrected " + getChartProperties().getSeriesName().toLowerCase(),
                data.getCorrectedPercentiles(), data.getCorrectedValues());

        responseTime.setLineColor(XChartSeriesColors.BLUE);
        responseTime.setMarkerColor(Color.LIGHT_GRAY);
        responseTime.setMarker(SeriesMarkers.NONE);
        responseTime.setLineStyle(SeriesLines.SOLID);

        encode(chart, fileName);
    }


    @Override
    public void plot(final HdrDataCO reportData, final File outputDir) throws MaestroException {
        HdrDataCoWrapper dataWrapper = new HdrDataCoWrapper(reportData);

        validateDataSet(dataWrapper.getCorrectedPercentiles(), dataWrapper.getCorrectedValues());
        validateDataSet(dataWrapper.getUncorrectedPercentiles(), dataWrapper.getUncorrectedValues());

        launchAsyncPlots(outputDir, dataWrapper);
    }
}

