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

import io.javalin.Context;
import org.apache.commons.io.FileUtils;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;

import java.io.File;
import java.io.FileInputStream;

public class RawFileController extends AbstractReportFileController {
    private final ReportDao reportDao = new ReportDao();

    @Override
    public void handle(Context context) throws Exception {
        try {
            int id = Integer.parseInt(context.param("id"));
            String name = context.param("name");

            Report report = reportDao.fetch(id);

            File file = getReportFile(report, name);

            context.result(new FileInputStream(file))
                    .header("Content-Length: ", String.valueOf(FileUtils.sizeOf(file)))
                    .header("Content-Type", "application/octet-stream");
        }
        catch (DataNotFoundException e) {
            context.status(404);
            context.result(String.format("Not found: %s", e.getMessage()));
        }
        catch (Throwable t) {
            context.status(500);
            context.result(String.format("Internal server error: %s", t.getMessage()));
        }
    }
}
