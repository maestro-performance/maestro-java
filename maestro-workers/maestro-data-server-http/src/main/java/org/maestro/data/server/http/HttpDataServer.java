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

package org.maestro.data.server.http;

import org.apache.commons.configuration.AbstractConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class HttpDataServer implements MaestroDataServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpDataServer.class);

    private static final int DEFAULT_DS_PORT = 0;

    private final File logDir;
    private Server server;
    private int dataServerPort;
    private final HandlerCollection contexts;
    private String host;

    /**
     * Constructor
     * @param logDir log directory to serve
     * @param host the hostname to bind this data server to or null for auto-detection or use the one from the config
     */
    public HttpDataServer(final File logDir, final String host) {
        this.logDir = logDir;

        this.contexts = new HandlerCollection(true);
        this.host = host;
    }

    private void runServerInt() throws Exception {
        AbstractConfiguration config = ConfigurationWrapper.getConfig();

        dataServerPort = config.getInteger("data.server.port", DEFAULT_DS_PORT);

        server = new Server(dataServerPort);
        server.setHandler(contexts);

        logger.debug("Starting the data server");
        server.start();

        addContext(Constants.TEST_LOGS_CONTEXT, logDir.getPath());
        addContext(Constants.WORKER_LOGS_CONTEXT, Constants.MAESTRO_LOG_DIR);


        if (dataServerPort == 0) {
            dataServerPort = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        }

        logger.info("The data server is now serving worker log files on {}{}", getServerURL(),
                Constants.WORKER_LOGS_CONTEXT);
        logger.info("The data server is now serving test log files on {}{}", getServerURL(),
                Constants.TEST_LOGS_CONTEXT);
    }

    /**
     * Adds a new context to be served via data server
     * @param contextPath the context path (ie.: /path/to/file)
     * @param location the location to serve
     * @throws Exception on multiple circumstances if unable to add the context root
     */
    private void addContext(final String contextPath, final String location) throws Exception {
        logger.debug("Serving files from location {} on context {}", location, contextPath);

        ResourceHandler resourceHandler = new ResourceHandler();

        URL resource = this.getClass().getResource("jetty-dir.css");
        if (resource != null) {
            resourceHandler.setStylesheet(resource.getPath());
        }
        else {
            logger.warn("Unable to find the CSS resource for the data server");
        }


        ContextHandler context = new ContextHandler();
        context.setContextPath(contextPath);
        context.addAliasCheck(new ContextHandler.ApproveAliases());

        context.setBaseResource(Resource.newResource(location));
        context.setHandler(resourceHandler);
        context.setServer(server);
        context.addAliasCheck(new ContextHandler.ApproveAliases());

        contexts.addHandler(context);
        context.start();
    }

    @Override
    public void run() {
        try {
            runServerInt();
            server.join();
        } catch (Exception e) {
            logger.error("Unable to start the data server: {}", e.getMessage(), e);
        }
    }


    /**
     * Gets the data server base URL. The front-end uses this to download the report files
     * @return the data server base URL.
     */
    public String getServerURL() {
        if (host == null) {
            AbstractConfiguration config = ConfigurationWrapper.getConfig();

            host = config.getString("data.server.host", null);

            // Host configuration takes priority over detection
            if (host == null) {
                host = ((ServerConnector) server.getConnectors()[0]).getHost();

                // If null, it's binding to all interfaces and that's OK
                if (host == null) {
                    try {
                        host = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        logger.error("Unable to determine the address of the data server local host. Please set it " +
                                "manually in the configuration file via 'data.server.host' setting. Using 127.0.0.1 ...");
                        host = "127.0.0.1";
                    }
                }
            }
        }

        return "http://" + host + ":" + dataServerPort;
    }
}
