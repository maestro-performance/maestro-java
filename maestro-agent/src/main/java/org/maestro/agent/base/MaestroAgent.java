package org.maestro.agent.base;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent for handle extension points
 */
public class MaestroAgent extends MaestroWorkerManager {

    private static final Logger logger = LoggerFactory.getLogger(MaestroAgent.class);
    private final GroovyHandler groovyHandler;
    private AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private final File path;


    /**
     * Constructor
     * @param maestroURL maestro_broker URL
     * @param role agent
     * @param host host address
     * @param dataServer data server object
     * @throws MaestroException if unable to create agent instance
     */
    public MaestroAgent(String maestroURL, String role, String host, MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);

        String pathStr = config.getString("maestro.agent.ext.path", null);

        if (pathStr == null){
            URL uri = this.getClass().getResource("/org/maestro/agent/ext/requests/");
            if (uri != null) {
                pathStr = uri.getPath();
            }
            else{
                logger.error("Unable to load files for extension points.");
                pathStr = Constants.HOME_DIR + File.separator + AgentConstants.EXTENSION_POINT;
            }
        }

        path = new File(pathStr);

        groovyHandler = new GroovyHandler(super.getClient());
    }

    /**
     * Start inspector handler
     * @param note StartInspector note
     */
    public void handle(StartInspector note) {
        File entryPointDir = new File(path, AgentConstants.START_INSPECTOR);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Start receiver handler
     * @param note StartReceiver note
     */
    public void handle(StartReceiver note) {
        File entryPointDir = new File(path, AgentConstants.START_RECEIVER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Start sender handler
     * @param note StartSender note
     */
    public void handle(StartSender note) {
        File entryPointDir = new File(path, AgentConstants.START_SENDER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop Inspector handler
     * @param note StopInspector note
     */
    public void handle(StopInspector note) {
        File entryPointDir = new File(path, AgentConstants.STOP_INSPECTOR);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop receiver handler
     * @param note StopReceiver note
     */
    public void handle(StopReceiver note) {
        File entryPointDir = new File(path, AgentConstants.STOP_RECEIVER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop sender handler
     * @param note StopSender note
     */
    public void handle(StopSender note) {
        File entryPointDir = new File(path, AgentConstants.STOP_SENDER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stats request handler
     * @param note Stats note
     */
    @Override
    public void handle(StatsRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.STATS);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Flush request handler
     * @param note Flush note
     */
    @Override
    public void handle(FlushRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.FLUSH);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Halt request handler
     * @param note Halt note
     */
    @Override
    public void handle(Halt note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.HALT);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Set request handler
     * @param note Set note
     */
    @Override
    public void handle(SetRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.SET);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Test failed notification handler
     * @param note NotifyFail note
     */
    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.NOTIFY_FAIL);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Test success notification handler
     * @param note NotifySuccess note
     */
    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.NOTIFY_SUCCESS);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Abnormal disconnection handler
     * @param note AbnormalDisconnect note
     */
    @Override
    public void handle(AbnormalDisconnect note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.ABNORMAL_DISCONNECT);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Ping request handler
     * @param note Ping note
     * @throws MaestroConnectionException if host is unreachable
     * @throws MalformedNoteException if note is malformed
     */
    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.PING);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Get request handler
     * @param note Get note
     */
    @Override
    public void handle(GetRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, AgentConstants.GET);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Callbacks wrapper for execute external points scripts
     * @param entryPointDir Path to external point dir
     */
    private void callbacksWrapper(File entryPointDir) {
        try {
            groovyHandler.setInitialPath(entryPointDir);
            groovyHandler.runCallbacks();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error during callback execution: {}", e.getMessage(), e);
            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError());
        }
    }

}
