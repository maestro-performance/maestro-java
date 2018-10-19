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

package org.maestro.reports.controllers;

import io.javalin.Context;
import io.javalin.Handler;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dto.Report;

import java.util.List;

public class AllAggregatedReportsController implements Handler {
    private final ReportDao reportDao = new ReportDao();

    @Override
    public void handle(Context context) throws Exception {
        List<Report> reports = reportDao.fetchAllAggregated();

        context.json(reports);
    }
}
