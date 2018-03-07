package org.maestro.agent.base;

import org.maestro.client.exceptions.MalformedNoteException;
import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroConnectionException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import org.apache.log4j.Logger;

//import javax.naming.Binding;

public class MaestroAgent extends MaestroWorkerManager {


    private static final String HOUSE="house";
    private static final String DETAILS="details";
    private static final String STATUS_OK="OK";
    private static final String STATUS_ERROR="ERROR";
    private static final Logger log = Logger.getLogger(MaestroAgent.class);


    public MaestroAgent(String maestroURL, String role, String host, MaestroDataServer dataServer) throws MaestroException {
        super(maestroURL, role, host, dataServer);
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
        log.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

        try {
            executeScript("../groovy", "Hello.groovy");
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     *
     * @param path
     * @param script
     * @throws IOException
     * @throws ResourceException
     * @throws ScriptException
     */
    private void executeScript(String path, String script) throws IOException, ResourceException, ScriptException {

        Binding binding = new Binding();
        GroovyScriptEngine engine = new GroovyScriptEngine(path);

        Object ret = engine.run(script, binding);

        if(STATUS_OK.equals(ret.toString())){
            if(binding.hasVariable(DETAILS)){
                Object details = binding.getVariable(DETAILS);
                System.out.println(details);
            }
        }else{
            log.warn("Something wrong in groovy");
        }

    }
}
