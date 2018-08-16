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

package org.maestro.reports.plotter;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.files.MptReportFile;
import org.maestro.reports.files.ReportFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public interface PlotterWrapper {
    boolean plot(final File file) throws MaestroException;

    default void handlePlotException(final File file, final Throwable t) {
        Logger logger = LoggerFactory.getLogger(PlotterWrapper.class);

        ReportFile reportFile = new MptReportFile(null, file);
        reportFile.setReportSuccessful(false);
        reportFile.setReportFailure(t);

        Properties prop = new Properties();

        prop.setProperty("error", "true");

        if (t != null) {
            logger.error("Unable to plot report file {}: {}", file.getPath(), t.getMessage());
            logger.trace("Exception: ", t);

            String message = t.getMessage();

            if (message != null) {
                prop.setProperty("message", message);
            }
            else {
                prop.setProperty("message", "Undefined error: " + t.getClass());
            }
        }
        else {
            logger.error("Unable to plot report file {}", file.getPath());
            prop.setProperty("message", "Undefined error");
        }
    }
}
