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

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.colors.ChartColor;
import org.maestro.plotter.common.ReportData;

import java.awt.*;

public abstract class DefaultHistogramPlotter<T extends ReportData> extends AbstractPlotter<T> {

    protected XYChart buildCommonChart() {

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
        chart.getStyler().setChartTitleFont(defaultFont.deriveFont(Font.BOLD).deriveFont(14));
        chart.getStyler().setLegendFont(defaultFont);
        chart.getStyler().setAxisTitleFont(defaultFont);
        chart.getStyler().setAxisTickLabelsFont(defaultFont.deriveFont(10));

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Vertical);

        return chart;
    }
}
