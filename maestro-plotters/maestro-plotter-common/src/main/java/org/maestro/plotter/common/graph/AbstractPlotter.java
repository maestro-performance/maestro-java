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


import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A base class for HDR plotters
 */
@SuppressWarnings("FieldCanBeLocal")
public abstract class AbstractPlotter<T> {
    private int outputWidth = 1200;
    private int outputHeight = 700;
    private boolean plotGridLinesVisible = true;

    private ChartProperties chartProperties = new ChartProperties();

    /**
     * Gets the output width
     * @return the output width
     */
    public int getOutputWidth() {
        return outputWidth;
    }


    /**
     * Gets the output height
     * @return the output height
     */
    public int getOutputHeight() {
        return outputHeight;
    }


    /**
     * Checks whether the grid lines are set to visible or not
     * @return true if visible or false otherwise
     */
    public boolean isPlotGridLinesVisible() {
        return plotGridLinesVisible;
    }

    /**
     * Get the chart properties
     * @return The chart properties object
     */
    public ChartProperties getChartProperties() {
        return chartProperties;
    }

    protected void validateDataSet(final List<?> xData, final List<?> yData) throws EmptyDataSet, IncompatibleDataSet {
        if (xData == null || xData.size() == 0) {
            throw new EmptyDataSet("The 'X' column data set is empty");
        }

        if (yData == null || yData.size() == 0) {
            throw new EmptyDataSet("The 'Y' column data set is empty");
        }

        if (xData.size() != yData.size()) {
            throw new IncompatibleDataSet("The data set size does not match");
        }
    }

    protected void updateChart(final String title, final String seriesName, final String xTitle, final String yTitle) {
        getChartProperties().setTitle(title);
        getChartProperties().setSeriesName(seriesName);
        getChartProperties().setxTitle(xTitle);
        getChartProperties().setyTitle(yTitle);
    }

    protected void encode(Chart<?, ?> chart, File outputFile) {
        try {
            BitmapEncoder.saveBitmap(chart, outputFile.getPath(), BitmapEncoder.BitmapFormat.PNG);
        }
        catch (IOException e) {
            throw new MaestroException(e);
        }
    }

    protected XYChart baseChart() {

        // Create Chart
        XYChart chart = new XYChartBuilder()
                .width(getOutputWidth())
                .height(getOutputHeight())
                .title(getChartProperties().getTitle())
                .xAxisTitle(getChartProperties().getxTitle())
                .yAxisTitle(getChartProperties().getyTitle())
                .theme(Styler.ChartTheme.Matlab)
                .build();

        chart.getStyler().setPlotBackgroundColor(ChartColor.getAWTColor(ChartColor.WHITE));
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setChartTitleBoxBackgroundColor(new Color(0, 222, 0));

        chart.getStyler().setPlotGridLinesVisible(isPlotGridLinesVisible());
        chart.getStyler().setXAxisLabelRotation(45);

        chart.getStyler().setAxisTickMarkLength(15);
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);
        chart.getStyler().setDatePattern("yyyy-MM-dd HH:mm:ss");

        Font defaultFont = new Font("Verdana", Font.PLAIN, 12);

        chart.getStyler().setBaseFont(defaultFont);
        chart.getStyler().setChartTitleFont(defaultFont.deriveFont(Font.BOLD).deriveFont(14.0F));
        chart.getStyler().setLegendFont(defaultFont);
        chart.getStyler().setAxisTitleFont(defaultFont);
        chart.getStyler().setAxisTickLabelsFont(defaultFont.deriveFont(10.0F));

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);

        return chart;
    }


    abstract public void plot(final T reportData, final File outputFile) throws MaestroException;
}
