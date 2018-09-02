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

package org.maestro.plotter.amqp.inspector.graph;

import org.junit.Test;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkDataSet;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkProcessor;
import org.maestro.plotter.amqp.inspector.routerlink.RouterLinkReader;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class RouterLinkPlotterTest {
    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/routerLink.csv").getPath();

        RouterLinkProcessor routerLinkProcessor = new RouterLinkProcessor();
        RouterLinkReader routerLinkReader = new RouterLinkReader(routerLinkProcessor);

        RouterLinkDataSet routerLinkDataSet = routerLinkReader.read(fileName);

        File sourceFile = new File(fileName);
        RouterLinkPlotter plotter = new RouterLinkPlotter();

        File outputFile = sourceFile.getParentFile();
        plotter.plot(routerLinkDataSet, outputFile);

        assertTrue("The output file does not exist: " + outputFile.getPath(), outputFile.exists());
    }
}
