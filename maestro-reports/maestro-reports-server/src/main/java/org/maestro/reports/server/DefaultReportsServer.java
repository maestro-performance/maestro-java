/*
 * Copyright 2018 Otavio Rodolfo Piske
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

package org.maestro.reports.server;

import io.javalin.Javalin;
import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.reports.controllers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DefaultReportsServer implements ReportsServer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsServer.class);
    private final File dataDir;
    private Javalin app;

    public DefaultReportsServer(final File dataDir) {
        this.dataDir = dataDir;
    }

    public void start() {
        if (app != null) {
            throw new MaestroException("Reports server is already started");
        }

        AbstractConfiguration config = ConfigurationWrapper.getConfig();

        final int port = config.getInteger("maestro.reports.server", 6500);

         app = Javalin.create()
                .port(port)
                .enableStaticFiles("/site")
                .enableCorsForAllOrigins()
                .disableStartupBanner()
                .start();

        app.get("/api/live", ctx -> ctx.result("Hello World"));

        // Common usage
        app.get("/api/report/", new AllReportsController());

        // For the report/node specific view
        app.get("/api/report/report/:id", new ReportController());
        app.get("/api/report/report/:id/properties", new ReportPropertiesController());
        app.get("/api/report/latency/all/report/:id", new LatencyReportController());
        app.get("/api/report/latency/statistics/report/:id", new LatencyStatisticsReportController());
        app.get("/api/report/rate/report/:id", new RateReportController());
        app.get("/api/report/rate/statistics/report/:id", new RateStatisticsReportController());

        // For all tests ... context needs to be adjusted
        app.get("/api/report/test/:test/number/:number/properties", new TestPropertiesController());

        // Aggregated
        app.get("/api/report/latency/aggregated/test/:id/number/:number", new AggregatedLatencyReportController());
        app.get("/api/report/latency/aggregated/statistics/test/:id/number/:number", new AggregatedLatencyStatisticsReportController());
        app.get("/api/report/rate/:role/aggregated/test/:id/number/:number", new AggregatedRateReportController());
        app.get("/api/report/rate/:role/aggregated/statistics/test/:id/number/:number", new AggregatedRateStatisticsReportController());
    }


    public void stop() {
        try {
            logger.info("Stopping the reports server");
            if (app != null) {
                app.stop();
            }
        }
        finally {
            app = null;
        }
    }


}
