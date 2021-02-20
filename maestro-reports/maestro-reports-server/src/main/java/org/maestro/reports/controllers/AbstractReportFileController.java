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

package org.maestro.reports.controllers;

import io.javalin.Handler;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.controllers.common.exceptions.ReportFileNotFound;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class AbstractReportFileController implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractReportFileController.class);

    protected File getReportFile(final Report report, final String name) {
        final File reportDir = new File(report.getLocation());

        File file = new File(reportDir, name);
        if (!file.exists()) {
            logger.error("Report file {} was not found on the report directory", file);
            throw new ReportFileNotFound("Report file %s was not found on the report directory", name);
        }

        if (!file.isFile()) {
            logger.error("Invalid report file: object {} is not a file", file);
            throw new MaestroException("Invalid report file: object %s is not a file", name);
        }

        return file;
    }
}
