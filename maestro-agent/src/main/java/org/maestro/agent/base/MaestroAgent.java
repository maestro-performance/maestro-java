package org.maestro.agent.base;

import org.maestro.client.exceptions.MalformedNoteException;
import org.maestro.client.notes.*;
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


    public MaestroAgent(String maestroURL, String role, String host, MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);
        groovyHandler = new GroovyHandler();
    }

    public void handle(StartInspector note) {

    }

    public void handle(StartReceiver note) {

    }

    public void handle(StartSender note) {

    }

    public void handle(StopInspector note) {

    }

    public void handle(StopReceiver note) {

    }

    public void handle(StopSender note) {

    }

    @Override
    public void handle(StatsRequest note) {
        super.handle(note);
    }

    @Override
    public void handle(FlushRequest note) {
        super.handle(note);

        URL uri = this.getClass().getResource("/org/maestro/agent/ext/requests/flush/");
        if (uri != null) {
            String path = uri.getPath();
            File file = new File(path);
            try {
                groovyHandler.setInitialPath(file);
                groovyHandler.runCallbacks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            logger.error("Unable to load files for extension points.");
        }


    }

    @Override
    public void handle(Halt note) {
        super.handle(note);
    }

    @Override
    public void handle(SetRequest note) {
        super.handle(note);
    }

    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        super.handle(note);
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        super.handle(note);

    }

    @Override
    public void handle(GetRequest note) {
        super.handle(note);
    }

}
