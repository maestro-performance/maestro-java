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

import net.orpiske.mpt.reports.MptReportFile;
import net.orpiske.mpt.reports.ReportFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public interface Plotter {
    boolean plot(File file);

    default void handlePlotException(File file, Throwable t) {
        Logger logger = LoggerFactory.getLogger(Plotter.class);

        logger.error("Unable to plot report file {}: {}", file.getPath(), t.getMessage());
        logger.trace("Exception: ", t);

        ReportFile reportFile = new MptReportFile(file);
        reportFile.setReportSuccessful(false);
        reportFile.setReportFailure(t);

        Properties prop = new Properties();

        prop.setProperty("error", "true");
        prop.setProperty("message", t.getMessage());
    }
}
