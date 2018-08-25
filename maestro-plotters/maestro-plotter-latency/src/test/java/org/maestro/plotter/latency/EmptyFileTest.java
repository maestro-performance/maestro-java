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
 *
 */

package org.maestro.plotter.latency;

import org.HdrHistogram.Histogram;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.maestro.plotter.common.exceptions.EmptyDataSet;
import org.maestro.plotter.latency.common.HdrData;
import org.maestro.plotter.latency.graph.HdrPlotter;
import org.maestro.plotter.latency.properties.HdrPropertyWriter;
import org.maestro.plotter.utils.Util;

import java.io.File;

public class EmptyFileTest {

    private void plot(String fileName) throws Exception {
        // HDR Log Reader
        HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper();

        File sourceFile = new File(fileName);
        Histogram histogram = Util.getAccumulated(sourceFile);

        HdrData hdrData = processorWrapper.convertLog(histogram);

        HdrPlotter plotter = new HdrPlotter(FilenameUtils.removeExtension(sourceFile.getName()));

        plotter.plot(hdrData, sourceFile.getParentFile());

        HdrPropertyWriter hdrPropertyWriter = new HdrPropertyWriter();

        hdrPropertyWriter.postProcess(histogram, sourceFile);

    }


    @Test(expected = EmptyDataSet.class)
    public void testPlot() throws Exception {
        String fileName = this.getClass().getResource("empty.hdr").getPath();
        plot(fileName);
    }
}
