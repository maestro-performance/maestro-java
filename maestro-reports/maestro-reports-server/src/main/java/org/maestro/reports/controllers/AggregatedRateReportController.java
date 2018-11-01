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
import org.maestro.plotter.common.serializer.SingleData;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;

import java.util.Objects;

public class AggregatedRateReportController extends CommonRateReportController {
    private final ReportDao reportDao = new ReportDao();

    @Override
    public void handle(Context context) {
        try {
            int testId = Integer.parseInt(
                    Objects.requireNonNull(context.param("id"), "The ID must be provided"));

            int testNumber = Integer.parseInt(
                    Objects.requireNonNull(context.param("number"), "The test number must be provided"));

            String hostRole = context.param("role");

            Report report = reportDao.fetchAggregated(testId, testNumber);

            SingleData<Long> rateData = processReport(report, hostRole);

            RateResponse rateResponse = new RateResponse();
            rateResponse.setPeriods(rateData.getPeriods());
            rateResponse.setRate(rateData.getValues());

            context.json(rateResponse);
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
