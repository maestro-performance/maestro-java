package org.maestro.agent.base;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.Constants;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

//import javax.naming.Binding;

public class MaestroAgent extends MaestroWorkerManager {

    private static final Logger logger = Logger.getLogger(MaestroAgent.class);
    private GroovyHandler groovyHandler;
    private AbstractConfiguration config = ConfigurationWrapper.getConfig();
    private File path;


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
                pathStr = Constants.HOME_DIR + File.separator + "ext";
            }
        }

        path = new File(pathStr);

        groovyHandler = new GroovyHandler(super.getClient());
    }

    public void handle(StartInspector note) {
        File entryPointDir = new File(path, "startinspector");
        callbacksWrapper(entryPointDir);
    }

    public void handle(StartReceiver note) {
        File entryPointDir = new File(path, "startreceiver");
        callbacksWrapper(entryPointDir);
    }

    public void handle(StartSender note) {
        File entryPointDir = new File(path, "startsender");
        callbacksWrapper(entryPointDir);
    }

    public void handle(StopInspector note) {
        File entryPointDir = new File(path, "stopinspector");
        callbacksWrapper(entryPointDir);
    }

    public void handle(StopReceiver note) {
        File entryPointDir = new File(path, "stopreceiver");
        callbacksWrapper(entryPointDir);
    }

    public void handle(StopSender note) {
        File entryPointDir = new File(path, "stopsender");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(StatsRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, "stats");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(FlushRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, "flush");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(Halt note) {
        super.handle(note);

        File entryPointDir = new File(path, "halt");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, "set");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);

        File entryPointDir = new File(path, "notifyfail");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);

        File entryPointDir = new File(path, "notifysuccess");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        super.handle(note);

        File entryPointDir = new File(path, "abnormaldisconnect");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        super.handle(note);

        File entryPointDir = new File(path, "pingrequest");
        callbacksWrapper(entryPointDir);
    }

    @Override
    public void handle(GetRequest note) {
        super.handle(note);

        File entryPointDir = new File(path, "get");
        callbacksWrapper(entryPointDir);
    }

    private void callbacksWrapper(File entryPointDir) {
        try {
            groovyHandler.setInitialPath(entryPointDir);
            groovyHandler.runCallbacks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
