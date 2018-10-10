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
import org.maestro.reports.controllers.AllReportsController;
import org.maestro.reports.controllers.LatencyReportController;
import org.maestro.reports.controllers.ReportController;
import org.maestro.reports.controllers.TestPropertiesController;

import java.io.File;

public class DefaultReportsServer implements ReportsServer {
    private final File dataDir;

    public DefaultReportsServer(final File dataDir) {
        this.dataDir = dataDir;
    }

    public void start() {
        AbstractConfiguration config = ConfigurationWrapper.getConfig();

        final int port = config.getInteger("maestro.reports.server", 6500);

        Javalin app = Javalin.create()
                .port(port)
                .enableStaticFiles("/site")
                .enableCorsForAllOrigins()
                .disableStartupBanner()
                .start();

        app.get("/api/live", ctx -> ctx.result("Hello World"));
        app.get("/api/report/", new AllReportsController());
        app.get("/api/report/report/:id", new ReportController());
        app.get("/api/report/test/:test/number/:number/properties", new TestPropertiesController());
        app.get("/api/report/latency/all/report/:id", new LatencyReportController());

    }


}
