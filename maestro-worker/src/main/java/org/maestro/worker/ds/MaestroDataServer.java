package org.maestro.worker.ds;

import org.apache.commons.configuration.AbstractConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.resource.Resource;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MaestroDataServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroDataServer.class);
    private static final int DEFAULT_DS_PORT = 8000;

    private Server server;
    private File logDir;

    public MaestroDataServer(final File logDir) {
        this.logDir = logDir;
    }

    private void runServerInt() throws Exception {
        AbstractConfiguration config = ConfigurationWrapper.getConfig();
        int dataServerPort = config.getInteger("data.server.port", DEFAULT_DS_PORT);
        server = new Server(dataServerPort);

        logger.info("Starting the data server on 0.0.0.0:{} to serve log files", DEFAULT_DS_PORT);


        ResourceHandler logResourceHandler = new ResourceHandler();

        // Serve the logs on /logs
        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/logs");
        context0.addAliasCheck(new ContextHandler.ApproveAliases());

        context0.setBaseResource(Resource.newResource(logDir));
        context0.setHandler(logResourceHandler);
        logger.info("Serving files from {} on /logs", logDir.getPath());

        ResourceHandler workerResourceHandler = new ResourceHandler();

        // Serve the worker logs on /worker
        ContextHandler context1 = new ContextHandler();
        context1.setContextPath("/worker");
        context1.addAliasCheck(new ContextHandler.ApproveAliases());

        context1.setBaseResource(Resource.newResource(Constants.MAESTRO_LOG_DIR));
        context1.setHandler(workerResourceHandler);
        logger.info("Serving files from {} on /worker", Constants.MAESTRO_LOG_DIR);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context0, context1 });

        server.setHandler(contexts);

        server.start();
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
}
