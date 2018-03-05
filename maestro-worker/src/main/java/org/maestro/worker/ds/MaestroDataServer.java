package org.maestro.worker.ds;

import org.apache.commons.configuration.AbstractConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.resource.Resource;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MaestroDataServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MaestroDataServer.class);

    private static final String WORKER_LOGS_CONTEXT = "/logs/worker";
    private static final String TEST_LOGS_CONTEXT = "/logs/tests";

    private static final int DEFAULT_DS_PORT = 0;

    private final File logDir;
    private Server server;
    private int dataServerPort;
    private final HandlerCollection contexts;

    /**
     * Constructor
     * @param logDir log directory to serve
     */
    public MaestroDataServer(final File logDir) {
        this.logDir = logDir;

        contexts = new HandlerCollection(true);
    }

    private void runServerInt() throws Exception {
        AbstractConfiguration config = ConfigurationWrapper.getConfig();

        dataServerPort = config.getInteger("data.server.port", DEFAULT_DS_PORT);

        server = new Server(dataServerPort);
        server.setHandler(contexts);

        logger.debug("Starting the data server");
        server.start();

        addContext(TEST_LOGS_CONTEXT, logDir.getPath());
        addContext(WORKER_LOGS_CONTEXT, Constants.MAESTRO_LOG_DIR);


        if (dataServerPort == 0) {
            dataServerPort = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
        }

        logger.info("The data server is now serving worker log files on {}{}", getServerURL(), WORKER_LOGS_CONTEXT);
        logger.info("The data server is now serving test log files on {}{}", getServerURL(), TEST_LOGS_CONTEXT);
    }

    /**
     * Adds a new context to be served via data server
     * @param contextPath the context path (ie.: /path/to/file)
     * @param location the location to serve
     * @throws Exception
     */
    public void addContext(final String contextPath, final String location) throws Exception {
        logger.debug("Serving files from location {} on context {}", contextPath, location);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setStylesheet(this.getClass().getResource("jetty-dir.css").getPath());

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

    /**
     * Removes a context from the data server
     * @param contextPath the context path to remove
     */
    public void removeContext(final String contextPath) {
        logger.info("Trying to add a new handler after start");

        // server.getHandler
        for (Handler handler : contexts.getHandlers()) {
            if (handler instanceof ContextHandler) {
                ContextHandler contextHandler = (ContextHandler) handler;

                if (contextPath.equals(contextHandler.getContextPath())) {
                    contexts.removeHandler(handler);
                }
            }
        }
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
        AbstractConfiguration config = ConfigurationWrapper.getConfig();
        String host = config.getString("data.server.host", null);

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

        return "http://" + host + ":" + dataServerPort;
    }
}
