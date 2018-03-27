package org.maestro.agent.base;

import org.apache.commons.configuration.AbstractConfiguration;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.NullProgressMonitor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.UUID;

/**
 * Agent for handle extension points. It implements everything that there is because it servers as a scriptable
 * extension that can act based on any maestro command
 */
public class MaestroAgent extends MaestroWorkerManager implements MaestroAgentEventListener, MaestroSenderEventListener,
        MaestroReceiverEventListener, MaestroInspectorEventListener
{

    private static final Logger logger = LoggerFactory.getLogger(MaestroAgent.class);
    private final GroovyHandler groovyHandler;
    private AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private File extPointPath;
    private Thread thread;


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

        extPointPath = new File(pathStr);

        groovyHandler = new GroovyHandler(super.getClient());
    }

    /**
     * Start inspector handler
     * @param note StartInspector note
     */
    @Override
    public void handle(StartInspector note) {
        File entryPointDir = new File(extPointPath, AgentConstants.START_INSPECTOR);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Start receiver handler
     * @param note StartReceiver note
     */
    @Override
    public void handle(StartReceiver note) {
        File entryPointDir = new File(extPointPath, AgentConstants.START_RECEIVER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Start sender handler
     * @param note StartSender note
     */
    @Override
    public void handle(StartSender note) {
        File entryPointDir = new File(extPointPath, AgentConstants.START_SENDER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop Inspector handler
     * @param note StopInspector note
     */
    @Override
    public void handle(StopInspector note) {
        File entryPointDir = new File(extPointPath, AgentConstants.STOP_INSPECTOR);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop receiver handler
     * @param note StopReceiver note
     */
    @Override
    public void handle(StopReceiver note) {
        File entryPointDir = new File(extPointPath, AgentConstants.STOP_RECEIVER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stop sender handler
     * @param note StopSender note
     */
    public void handle(StopSender note) {
        File entryPointDir = new File(extPointPath, AgentConstants.STOP_SENDER);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Stats request handler
     * @param note Stats note
     */
    @Override
    public void handle(StatsRequest note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.STATS);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Flush request handler
     * @param note Flush note
     */
    @Override
    public void handle(FlushRequest note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.FLUSH);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Halt request handler
     * @param note Halt note
     */
    @Override
    public void handle(Halt note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.HALT);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Set request handler
     * @param note Set note
     */
    @Override
    public void handle(SetRequest note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.SET);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Test failed notification handler
     * @param note NotifyFail note
     */
    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.NOTIFY_FAIL);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Test success notification handler
     * @param note NotifySuccess note
     */
    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.NOTIFY_SUCCESS);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Abnormal disconnection handler
     * @param note AbnormalDisconnect note
     */
    @Override
    public void handle(AbnormalDisconnect note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.ABNORMAL_DISCONNECT);
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

        File entryPointDir = new File(extPointPath, AgentConstants.PING);
        callbacksWrapper(entryPointDir);
    }

    /**
     * Get request handler
     * @param note Get note
     */
    @Override
    public void handle(GetRequest note) {
        super.handle(note);

        File entryPointDir = new File(extPointPath, AgentConstants.GET);
        callbacksWrapper(entryPointDir);
    }


    /**
     * Callbacks wrapper for execute external points scripts
     * @param entryPointDir Path to external point dir
     */
    private void callbacksWrapper(File entryPointDir) {
        try {

            groovyHandler.setInitialPath(entryPointDir);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        System.out.println("Executing groovyHandler by thread: " + this.getClass().getName());
                        groovyHandler.runCallbacks();

                    }
                    catch (Exception e) {
                        groovyHandler.getClient().notifyFailure(this.getClass().getName());
                    }
                    finally {
                        groovyHandler.getClient().notifySuccess(this.getClass().getName());
                    }
                }
            });

            thread.start();

            this.getClient().replyOk();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error during callback execution: {}", e.getMessage(), e);
            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError());
        }
    }

    /**
     * Start agent handler
     * @param note Start Agent note
     */
    @Override
    public void handle(StartAgent note) {

    }

    /**
     * Stop agent handler
     * @param note Stop Agent note
     */
    @Override
    public void handle(StopAgent note) {

    }

    // @TODO jstejska: move this into agent somehow?
    @Override
    public void handle(AgentGeneralRequest note) {
        logger.info("Execute request arrived");

        File entryPointDir = new File(extPointPath, note.getValue());
        callbacksWrapper(entryPointDir);

        AgentGeneralResponse response = new AgentGeneralResponse();
        // @TODO jstejska: status should be set in groovy handler script I guess
        response.setStatus("OK");
        getClient().AgentGeneralResponse(response);
    }

    @Override
    public void handle(AgentSourceRequest note) {
        logger.info("Source request arrived");

        final String sourceUrl = note.getSourceUrl();
        logger.info("Preparing to download code from {}", sourceUrl);
        final String tmpPath = config.getString("maestro.agent.source.root", "/tmp/maestro/agent/work");
        final String projectDir = UUID.randomUUID().toString();

        File repositoryDir = new File(tmpPath + File.separator + projectDir + File.separator);

        if (!repositoryDir.exists()) {
            if (!repositoryDir.mkdirs()) {
                logger.warn("Unable to create directory: {}", repositoryDir);
            }
        }

        CloneCommand cloneCommand = Git.cloneRepository();

        cloneCommand.setURI(sourceUrl);
        cloneCommand.setDirectory(repositoryDir);
        cloneCommand.setProgressMonitor(NullProgressMonitor.INSTANCE);

        try {
            cloneCommand.call();
            logger.info("Source directory for project created at {}", repositoryDir);
            extPointPath = new File(repositoryDir, "requests");

            getClient().replyOk();
        } catch (GitAPIException e) {
            logger.error("Unable to clone repository: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }
        finally {
            if (repositoryDir != null) {
                repositoryDir.deleteOnExit();
            }
        }
    }
}
