/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.reports.plotter;

import net.orpiske.hhp.plot.HdrData;
import net.orpiske.hhp.plot.HdrLogProcessorWrapper;
import net.orpiske.hhp.plot.HdrReader;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HdrPlotter implements Plotter {
    private static Logger logger = LoggerFactory.getLogger(HdrPlotter.class);

    private HdrLogProcessorWrapper processorWrapper = new HdrLogProcessorWrapper();
    private HdrReader reader = new HdrReader();

    @Override
    public boolean plot(File file) {
        try {
            String csvFile = processorWrapper.convertLog(file.getPath());

            // CSV Reader
            HdrData hdrData = reader.read(csvFile);

            // HdrPlotter
            net.orpiske.hhp.plot.HdrPlotter plotter = new net.orpiske.hhp.plot.HdrPlotter(FilenameUtils.removeExtension(file.getPath()));

            plotter.setOutputWidth(1024);
            plotter.setOutputHeight(600);
            plotter.plot(hdrData.getPercentile(), hdrData.getValue());

            return true;
        }
        catch (Throwable t) {
            String baseName = FilenameUtils.removeExtension(file.getPath());

            handlePlotException(file, t);
        }

        return false;
    }
}
