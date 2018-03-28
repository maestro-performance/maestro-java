package org.maestro.agent.base;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.io.FileUtils;
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
import java.util.LinkedList;
import java.util.List;
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
    private List<ExtensionPoint> extensionPoints = new LinkedList<>();
    private File extPointPath;
    private Thread thread;

    private final String sourceRoot;


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

        sourceRoot = config.getString("maestro.agent.source.root", FileUtils.getTempDirectoryPath());

        extensionPoints.add(new ExtensionPoint(new File(pathStr), false));
        groovyHandler = new GroovyHandler(super.getClient());
    }

    /**
     * Start inspector handler
     * @param note StartInspector note
     */
    @Override
    public void handle(StartInspector note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.START_INSPECTOR));
    }

    /**
     * Start receiver handler
     * @param note StartReceiver note
     */
    @Override
    public void handle(StartReceiver note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.START_RECEIVER));
    }

    /**
     * Start sender handler
     * @param note StartSender note
     */
    @Override
    public void handle(StartSender note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.START_SENDER));
    }

    /**
     * Stop Inspector handler
     * @param note StopInspector note
     */
    @Override
    public void handle(StopInspector note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.STOP_INSPECTOR));
    }

    /**
     * Stop receiver handler
     * @param note StopReceiver note
     */
    @Override
    public void handle(StopReceiver note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.STOP_RECEIVER));
    }

    /**
     * Stop sender handler
     * @param note StopSender note
     */
    public void handle(StopSender note) {
        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.STOP_SENDER));
    }

    /**
     * Stats request handler
     * @param note Stats note
     */
    @Override
    public void handle(StatsRequest note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.STATS));
    }

    /**
     * Flush request handler
     * @param note Flush note
     */
    @Override
    public void handle(FlushRequest note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.FLUSH));
    }

    /**
     * Halt request handler
     * @param note Halt note
     */
    @Override
    public void handle(Halt note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.HALT));
    }

    /**
     * Set request handler
     * @param note Set note
     */
    @Override
    public void handle(SetRequest note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.SET));
    }

    /**
     * Test failed notification handler
     * @param note NotifyFail note
     */
    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.NOTIFY_FAIL));
    }

    /**
     * Test success notification handler
     * @param note NotifySuccess note
     */
    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.NOTIFY_SUCCESS));
    }

    /**
     * Abnormal disconnection handler
     * @param note AbnormalDisconnect note
     */
    @Override
    public void handle(AbnormalDisconnect note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.ABNORMAL_DISCONNECT));
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

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  AgentConstants.PING));
    }

    /**
     * Get request handler
     * @param note Get note
     */
    @Override
    public void handle(GetRequest note) {
        super.handle(note);

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(), AgentConstants.GET));
    }


    /**
     * Callbacks wrapper for execute external points scripts
     * @param entryPointPath the root directory of the extension points
     */
    private void callbacksWrapper(final File entryPointPath, final String codeDir) {
        try {
            File entryPointDir = new File(entryPointPath, codeDir);

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

        extensionPoints.forEach(point -> callbacksWrapper(point.getPath(),  note.getValue());

        AgentGeneralResponse response = new AgentGeneralResponse();
        // @TODO jstejska: status should be set in groovy handler script I guess
        response.setStatus("OK");
        getClient().AgentGeneralResponse(response);
    }

    @Override
    public void handle(AgentSourceRequest note) {
        logger.info("Source request arrived");

        final String sourceUrl = note.getSourceUrl();
        final String branch = note.getBranch();

        if (branch == null) {
            logger.info("Preparing to download code from {}", sourceUrl);
        }
        else {
            logger.info("Preparing to download code from {} from branch {}", sourceUrl, branch);
        }
        final String projectDir = UUID.randomUUID().toString();

        File repositoryDir = new File(sourceRoot + File.separator + projectDir + File.separator);

        if (!repositoryDir.exists()) {
            if (!repositoryDir.mkdirs()) {
                logger.warn("Unable to create directory: {}", repositoryDir);
            }
        }

        CloneCommand cloneCommand = Git.cloneRepository();

        cloneCommand.setURI(sourceUrl);
        cloneCommand.setDirectory(repositoryDir);
        cloneCommand.setProgressMonitor(NullProgressMonitor.INSTANCE);


        if (branch != null) {
            cloneCommand.setBranch(branch);
        }

        try {
            cloneCommand.call();
            logger.info("Source directory for project created at {}", repositoryDir);
            extensionPoints.add(new ExtensionPoint(new File(repositoryDir, "requests"), true));

            getClient().replyOk();
        } catch (GitAPIException e) {
            logger.error("Unable to clone repository: {}", e.getMessage(), e);
            getClient().replyInternalError();
        }
    }
}
