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

import org.junit.Test;
import org.maestro.plotter.rate.RateData;
import org.maestro.plotter.rate.RateDataReader;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class RatePlotterTest {
    protected RateData getData(final String resource) throws Exception {
        final String fileName = this.getClass().getResource(resource).getPath();

        RateDataReader queueReader = new RateDataReader();
        return queueReader.read(fileName);
    }

    private void testPlot(final String resource, final String output) throws Exception {
        RateData rateData = getData(resource);

        RatePlotter ratePlotter = new RatePlotter();

        File sourceFile = new File(this.getClass().getResource(resource).getPath());
        File outputFile = new File(sourceFile.getParentFile(), output);

        ratePlotter.plot(rateData, outputFile);
        assertTrue("The output file does not exist", outputFile.exists());
    }

    @Test
    public void testSenderPlot() throws Exception {
        testPlot("/data-ok/sender.dat", "senderd-rate-01.png");
    }

    @Test
    public void testReceiverPlot() throws Exception {
        testPlot("/data-ok/receiver.dat", "receiverd-rate-01.png");
    }
}
