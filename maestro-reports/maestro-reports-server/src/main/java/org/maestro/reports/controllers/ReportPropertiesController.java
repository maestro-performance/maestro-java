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
import io.javalin.Handler;
import org.maestro.common.HostTypes;
import org.maestro.common.test.TestProperties;
import org.maestro.common.test.properties.PropertyReader;
import org.maestro.reports.controllers.common.ExtendedTestProperties;
import org.maestro.reports.dao.ReportDao;
import org.maestro.reports.dao.exceptions.DataNotFoundException;
import org.maestro.reports.dto.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReportPropertiesController implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(ReportPropertiesController.class);
    private final ReportDao reportDao = new ReportDao();

    public ReportPropertiesController() {
    }

    @Override
    public void handle(Context context) {
        try {
            int id = Integer.parseInt(context.param("id"));

            Report report = reportDao.fetch(id);

            String location = report.getLocation();
            File file = new File(location, "test.properties");

            TestProperties tp = new TestProperties();

            List<ExtendedTestProperties> testPropertiesList = new LinkedList<>();

            if (HostTypes.isWorker(report.getTestHostRole())) {
                PropertyReader reader = new PropertyReader();

                reader.read(file, tp);

                ExtendedTestProperties etp = new ExtendedTestProperties(tp);
                etp.setRole(report.getTestHostRole());
                testPropertiesList.add(etp);
                context.json(testPropertiesList);
            }
            else {
                context.status(500);
                context.result(String.format("Unhandled node type for the report: %s", report.getTestHostRole()));
            }
        }
        catch (DataNotFoundException e) {
            context.status(404);
            context.result(String.format("Not found: %s", e.getMessage()));
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
            context.status(500);
            context.result(String.format("Internal server error: %s", t.getMessage()));
        }
    }
}
