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

import org.junit.Before;
import org.junit.Test;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.common.exceptions.IncompatibleDataSet;
import org.maestro.plotter.rate.DefaultRateReader;
import org.maestro.plotter.rate.RateData;
import org.maestro.plotter.rate.RateDataProcessor;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class RatePlotterTest {
    private final String fileName = this.getClass().getResource("/data-ok/senderd-rate-01.csv.gz").getPath();
    private RateData rateData;

    @Before
    public void setUp() throws Exception {
        RateDataProcessor queueProcessor = new RateDataProcessor();
        DefaultRateReader queueReader = new DefaultRateReader(queueProcessor);

        rateData = queueReader.read(fileName);
    }

    @Test
    public void testSenderPlot() throws EmptyDataSet, IncompatibleDataSet, IOException {
        RatePlotter ratePlotter = new RatePlotter();

        File sourceFile = new File(fileName);
        File outputFile = new File(sourceFile.getParentFile(), "senderd-rate-01.png");

        ratePlotter.plot(rateData, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }
}
