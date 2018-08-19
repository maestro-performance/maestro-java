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

package org.maestro.inspector.base;

import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.worker.common.MaestroWorkerManager;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

public class InspectorManager extends MaestroWorkerManager implements MaestroInspectorEventListener {
    private static final Logger logger = LoggerFactory.getLogger(InspectorManager.class);
    private Thread inspectorThread;
    private MaestroInspector inspector;
    private final File logDir;
    private final HashMap<String, String> inspectorMap = new HashMap<>();
    private String url;

    public InspectorManager(final String maestroURL, final PeerInfo peerInfo, final MaestroDataServer dataServer, final File logDir) throws MaestroException
    {
        super(maestroURL, peerInfo, dataServer);

        this.logDir = logDir;

        inspectorMap.put("InterconnectInspector", "org.maestro.inspector.amqp.InterconnectInspector");
        inspectorMap.put("ArtemisInspector", "org.maestro.inspector.activemq.ArtemisInspector");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void createInspector(final StartInspector note, final String inspectorType) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<MaestroInspector> clazz = (Class<MaestroInspector>) Class.forName(inspectorType);

        inspector = clazz.newInstance();
        inspector.setEndpoint(getClient());
        inspector.setBaseLogDir(logDir);

        try {
            inspector.setUrl(url);
        } catch (MaestroException e) {
            logger.error("Unable to set the management interface URL {}: {}", url, e.getMessage(), e);
            getClient().replyInternalError(note,"Unable to set the management interface URL %s: %s", url,
                    e.getMessage());
        }
    }

    @Override
    public void handle(final StartInspector note) {
        logger.debug("Start inspector request received");

        try {
            createInspector(note, inspectorMap.get(note.getPayload()));

            inspector.setWorkerOptions(getWorkerOptions());

            InspectorContainer inspectorContainer = new InspectorContainer(inspector);

            inspectorThread = new Thread(inspectorContainer);
            inspectorThread.start();

            getClient().replyOk(note);
        }
        catch (Throwable t) {
            logger.error("Unable to start inspector: {}", t.getMessage(), t);
            getClient().replyInternalError(note, "Unable to start inspector: %s", t.getMessage());
        }
    }


    @Override
    public void handle(final SetRequest note) {
        super.handle(note);

        if (note.getOption() == SetRequest.Option.MAESTRO_NOTE_OPT_SET_MI) {
            String value = note.getValue();

            if (value != null) {
                setUrl(value);
            }
            else {
                logger.error("Unable to set management interface URL: null URL");
                getClient().replyInternalError(note,"Unable to set management interface URL: null URL");
            }
        }

        getClient().replyOk(note);

    }

    @Override
    public void handle(final StopInspector note) {
        logger.debug("Stop inspector request received");

        if (inspectorThread != null) {
            try {
                inspector.stop();
            } catch (Exception e) {
                logger.warn("Unable to stop the inspector in a clean way: {}", e.getMessage(), e);
            }

            inspectorThread.interrupt();
            inspectorThread = null;
        }
        else {
            logger.warn("Inspector received a stop request but it is not running");
        }

        getClient().replyOk(note);
    }

    @Override
    public void handle(TestFailedNotification note) {
        super.handle(note);

        if (inspectorThread != null) {
            logger.debug("Stopping the inspection as a result of a test failure notification by one of the peers");
            stopInspectorThread();
        }
    }



    @Override
    public void handle(TestSuccessfulNotification note) {
        super.handle(note);

        if (inspectorThread != null) {
            logger.debug("Stopping the inspection as a result of a test success notification by one of the peers");
            stopInspectorThread();
        }
    }

    private void stopInspectorThread() {
        try {
            inspector.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop the inspector in a clean way: {}", e.getMessage(), e);
        }

        try {
            inspectorThread.join(1000);
        } catch (InterruptedException e) {
            logger.debug("Interrupted the inspector thread", e);
        }

        if (inspectorThread.isAlive()) {
            logger.warn("The inspector thread is still alive. Forcing it to stop");

            inspectorThread.interrupt();
        }

        inspectorThread = null;
    }

    @Override
    public void handle(final LogRequest note) {
        super.handle(note, logDir);
    }

    @Override
    public void handle(RoleAssign note) {
        getClient().replyOk(note);
    }

    @Override
    public void handle(RoleUnassign note) {
        getClient().replyOk(note);
    }

    @Override
    public void handle(StartWorker note) {
        // NO-OP for now
    }

    @Override
    public void handle(StopWorker note) {
        // NO-OP for now
    }
}

