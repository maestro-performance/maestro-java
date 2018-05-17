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

import org.junit.Test;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.plotter.inspector.queues.QueueDataSet;
import org.maestro.plotter.inspector.queues.QueueProcessor;
import org.maestro.plotter.inspector.queues.QueueReader;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class QueuePlotterTest {

    @Test
    public void testPlot() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/data-ok/queues.csv").getPath();

        QueueProcessor queueProcessor = new QueueProcessor();
        QueueReader queueReader = new QueueReader(queueProcessor);

        QueueDataSet dataSet = queueReader.read(fileName);

        File sourceFile = new File(fileName);
        QueuePlotter plotter = new QueuePlotter();

        File outputFile = new File(sourceFile.getParentFile(), QueuePlotter.DEFAULT_FILENAME);
        plotter.plot(dataSet, outputFile);

        assertTrue("The output plotted file does not exist", outputFile.exists());
    }


    @Test(expected = EmptyDataSet.class)
    public void testPlotEmpty() throws IOException, EmptyDataSet, IncompatibleDataSet {
        String fileName = this.getClass().getResource("/empty/queues.csv").getPath();

        QueueProcessor queueProcessor = new QueueProcessor();
        QueueReader queueReader = new QueueReader(queueProcessor);

        QueueDataSet dataSet = queueReader.read(fileName);

        File sourceFile = new File(fileName);
        QueuePlotter plotter = new QueuePlotter();

        File outputFile = new File(sourceFile.getParentFile(), QueuePlotter.DEFAULT_FILENAME);
        plotter.plot(dataSet, outputFile);

        assertTrue("The output plotted file does not exist", outputFile.exists());
    }
}
