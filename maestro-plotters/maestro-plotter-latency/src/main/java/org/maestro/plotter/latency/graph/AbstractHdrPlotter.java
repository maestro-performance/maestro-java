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

import org.maestro.common.exceptions.MaestroException;
import org.maestro.plotter.common.graph.AbstractPlotter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.maestro.plotter.utils.Util.asyncPlot;

public abstract class AbstractHdrPlotter<T, Y> extends AbstractPlotter<Y> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHdrPlotter.class);
    private final String baseName;

    public AbstractHdrPlotter(final String baseName) {
        this.baseName = baseName;

        getChartProperties().setyTitle("Milliseconds");
        getChartProperties().setSeriesName("Percentiles");
    }

    public AbstractHdrPlotter(final String baseName, final String timeUnit) {
        this.baseName = baseName;

        getChartProperties().setyTitle(timeUnit);
        getChartProperties().setSeriesName("Percentiles");
    }

    protected abstract void plotDataAt(final T data, final Double min, final File fileName);

    private void plot99(final T data, final File outputDir) {
        plotDataAt(data, 99.0, new File(outputDir,baseName + "_99.png"));
    }

    private void plot90(final T data, final File outputDir) {
        plotDataAt(data, 90.0, new File(outputDir,baseName + "_90.png"));
    }

    private void plotAll(final T data, final File outputDir) {
        plotDataAt(data, 5.0, new File(outputDir,baseName + "_all.png"));
    }

    protected void launchAsyncPlots(File outputDir, T dataWrapper) {
        Future<?> plotterFuture = asyncPlot(this::plotAll, dataWrapper, outputDir);

        Future<?> plotter90Future = asyncPlot(this::plot90, dataWrapper, outputDir);

        Future<?> plotter99Future = asyncPlot(this::plot99, dataWrapper, outputDir);

        try {
            plotterFuture.get();
            plotter90Future.get();
            plotter99Future.get();
        } catch (InterruptedException e) {
            logger.error("Interrupted while plotting the data");
        } catch (ExecutionException e) {
            throw new MaestroException(e);
        }
    }
}
