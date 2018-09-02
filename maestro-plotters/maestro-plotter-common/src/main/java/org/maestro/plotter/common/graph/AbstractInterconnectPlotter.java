/*
 * Copyright 2018 Otavio Rodolfo Piske
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

import org.maestro.plotter.common.statistics.Statistics;

import java.io.File;
import java.util.Date;
import java.util.Map;


public abstract class AbstractInterconnectPlotter<T> extends DefaultScatterPlotter<T> {

    protected abstract String getDefaultName();

    private String friendlyName(final String areaName) {
        return getDefaultName() + areaName.replace(" ", "_").toLowerCase() + ".png";
    }

    protected void plot(final Map<Date, Statistics> reportData, final File outputDir, final String name) {
        final File outputFile = new File(outputDir, friendlyName(name));

        createChart(name, outputFile, reportData);
    }
}
