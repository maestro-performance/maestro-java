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
import org.maestro.reports.controllers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReportsServer implements ReportsServer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultReportsServer.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final Javalin app;

    public DefaultReportsServer() {
        app = Javalin.create();

        configure(app);

        registerUris(app);
    }

    protected void configure(final Javalin app) {
        final int port = config.getInteger("maestro.reports.server", 6500);

        app.port(port)
                .enableStaticFiles("/site")
                .enableCorsForAllOrigins()
                .disableStartupBanner();
    }

    protected Javalin getServerInstance() {
        return app;
    }

    protected void registerUris(final Javalin app) {
        logger.warn("Registering reports server URIs");

        app.get("/api/live", ctx -> ctx.result("Hello World"));

        // Common usage
        app.get("/api/report/", new AllReportsController());

        // Common usage
        app.get("/api/report/aggregated", new AllAggregatedReportsController());

        // For the report/node specific view
        app.get("/api/report/report/:id", new ReportController());
        app.get("/api/report/report/:id/properties", new ReportPropertiesController());
        app.get("/api/report/latency/all/report/:id", new LatencyReportController());
        app.get("/api/report/latency/statistics/report/:id", new LatencyStatisticsReportController());
        app.get("/api/report/rate/report/:id", new RateReportController());
        app.get("/api/report/rate/statistics/report/:id", new RateStatisticsReportController());

        // For all tests ... context needs to be adjusted
        app.get("/api/report/test/:test/number/:number/properties", new TestPropertiesController());
        app.get("/api/report/test/:test/sut/node", new SutNodeInfoController());

        // Aggregated
        app.get("/api/report/latency/aggregated/test/:id/number/:number", new AggregatedLatencyReportController());
        app.get("/api/report/latency/aggregated/statistics/test/:id/number/:number", new AggregatedLatencyStatisticsReportController());
        app.get("/api/report/rate/:role/aggregated/test/:id/number/:number", new AggregatedRateReportController());
        app.get("/api/report/rate/:role/aggregated/statistics/test/:id/number/:number", new AggregatedRateStatisticsReportController());

        app.get("/api/report/:id/files", new FileListController());
        app.get("/raw/report/:id/files/:name", new RawFileController());
    }

    public final void start() {
        logger.debug("Starting the reports server");
        app.start();
    }


    public void stop() {
        try {
            logger.info("Stopping the reports server");
            if (app != null) {
                app.stop();
            }
        }
        finally {

        }
    }


}
