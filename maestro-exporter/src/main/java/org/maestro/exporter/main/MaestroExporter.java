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

package org.maestro.exporter.main;


import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.Maestro;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.exporter.collectors.ConnectionCount;
import org.maestro.exporter.collectors.MessageCount;
import org.maestro.exporter.collectors.PingInfo;
import org.maestro.exporter.collectors.RateCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class MaestroExporter {
    private static final Logger logger = LoggerFactory.getLogger(MaestroExporter.class);
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private static final MessageCount messageCounter;
    private static final RateCount rateCounter;
    private static final ConnectionCount connectionCounter;

    private static final PingInfo pingInfo;

    private static final Counter failures;
    private static final Counter successes;
    private static final Counter abnormal;

    private Maestro maestro;

    static {
        messageCounter = MessageCount.getInstance();
        rateCounter = RateCount.getInstance();
        connectionCounter = ConnectionCount.getInstance();
        pingInfo = PingInfo.getInstance();

        failures = Counter.build()
                 .name("maestro_test_failures")
                 .help("Test failures")
                 .register();

        successes = Counter.build().name("maestro_test_success")
                .help("Test success")
                .register();

        abnormal = Counter.build().name("maestro_peer_abnormal_disconnect")
                .help("Abnormal disconnect count")
                .register();
    }

    public MaestroExporter(final String maestroUrl) throws MaestroException {
        maestro = new Maestro(maestroUrl);

        messageCounter.register();
        rateCounter.register();
        connectionCounter.register();
        pingInfo.register();
    }

    private void processNotes(List<? extends MaestroNote> notes) {
        for (MaestroNote note : notes) {
            if (note instanceof StatsResponse) {
                StatsResponse statsResponse = (StatsResponse) note;

                rateCounter.record(statsResponse);
                messageCounter.record(statsResponse);
                connectionCounter.record(statsResponse);
            }
            else {
                if (note instanceof PingResponse) {
                    PingResponse pingResponse = (PingResponse) note;

                    pingInfo.record(pingResponse);
                }
                else {
                   if (note instanceof TestFailedNotification) {
                       failures.inc();
                   }
                   else {
                       if (note instanceof TestSuccessfulNotification) {
                           successes.inc();
                       }
                       else {
                           if (note instanceof AbnormalDisconnect) {
                               abnormal.inc();
                           }
                       }
                   }
                }
            }

            logger.trace("Note: {}", note.toString());
        }
    }


    public int run(int port) throws MaestroConnectionException, IOException {
        logger.info("Exporting metrics on 0.0.0.0:{}", port);

        final int updateInterval = config.getInt("exporter.update.interval", 100000);
        HTTPServer server = null;

        try {
            server = new HTTPServer(port);

            boolean running = true;
            while (running) {
                logger.debug("Sending requests");
                CompletableFuture<List<? extends MaestroNote>> statsFutures = maestro.statsRequest();
                CompletableFuture<List<? extends MaestroNote>> pingFutures = maestro.pingRequest();

                List<? extends MaestroNote> stats = null;

                try {
                    stats = statsFutures.get();
                } catch (InterruptedException e) {
                    logger.debug("Interrupted. Aborting");
                    break;
                } catch (ExecutionException e) {
                    logger.error("Error executing the stats request: {}", e.getMessage(), e);
                }

                List<? extends MaestroNote> pings = null;
                try {
                    pings = pingFutures.get();
                } catch (InterruptedException e) {
                    logger.debug("Interrupted. Aborting");
                    break;
                } catch (ExecutionException e) {
                    logger.error("Error executing the stats request: {}", e.getMessage(), e);
                }


                if (stats != null) {
                    processNotes(stats);
                }

                if (pings != null) {
                    processNotes(pings);
                }

                try {
                    Thread.sleep(updateInterval);
                } catch (InterruptedException e) {
                    logger.debug("Interrupted. Stopping ...");
                    break;
                }
            }
        }
        finally {
            if (maestro != null) {
                logger.info("Stopping Maestro client");
                maestro.stop();
            }

            if (server != null) {
                logger.info("Stopping HTTP server");
                server.stop();
            }
        }

        return 0;
    }
}
