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

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.*;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.worker.common.MaestroWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class InspectorManager extends MaestroWorkerManager implements MaestroInspectorEventListener {
    private static final Logger logger = LoggerFactory.getLogger(InspectorManager.class);
    private ExecutorService inspectorExecutor;
    private InspectorContainer inspectorContainer;

    private final File logDir;
    private final HashMap<String, String> inspectorMap = new HashMap<>();
    private String url;

    private class InspectorThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "InspectorThread");
        }
    }

    public InspectorManager(MaestroClient maestroClient, ConsumerEndpoint consumerEndpoint, PeerInfo peerInfo, final File logDir) throws MaestroException
    {
        super(maestroClient, consumerEndpoint, peerInfo);

        this.logDir = logDir;

        inspectorMap.put("InterconnectInspector", "org.maestro.inspector.amqp.InterconnectInspector");
        inspectorMap.put("ArtemisInspector", "org.maestro.inspector.activemq.ArtemisInspector");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private MaestroInspector createInspector(final StartInspector note, final String inspectorType) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException
    {
        logger.info("Creating a {} inspector", inspectorType);

        @SuppressWarnings("unchecked")
        final Class<MaestroInspector> clazz = (Class<MaestroInspector>) Class.forName(inspectorType);

        if (clazz == null) {
            throw new MaestroException("There is no inspector recorded for %s", inspectorType);
        }

        MaestroInspector inspector = clazz.newInstance();
        logger.debug("A {} inspector was created successfully", inspectorType);

        return inspector;
    }

    private void setInspectorParameters(MaestroInspector inspector) throws DurationParseException {
        inspector.setEndpoint(getClient());
        inspector.setBaseLogDir(logDir);
        inspector.setUrl(url);
        inspector.setWorkerOptions(getWorkerOptions());
        inspector.setTest(getCurrentTest());
    }

    @Override
    public void handle(final StartInspector note) {
        logger.debug("Start inspector request received");

        try {
            inspectorExecutor = Executors.newSingleThreadExecutor(new InspectorThreadFactory());

            MaestroInspector inspector = createInspector(note, inspectorMap.get(note.getPayload()));

            setInspectorParameters(inspector);

            inspectorContainer = new InspectorContainer(inspector);

            inspectorExecutor.execute(inspectorContainer);

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
            final String value = note.getValue();

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
        getClient().replyOk(note);

        doInspectorStop();
        logger.info("Completed stopping the inspector");
    }

    private void doInspectorStop() {
        if (inspectorContainer ==  null) {
            return;
        }

        inspectorContainer.stop();

        if (inspectorExecutor != null) {

            final AbstractConfiguration config = ConfigurationWrapper.getConfig();
            int interval = config.getInteger("inspector.sleep.interval", 5000) + 1000;

            try {
                inspectorExecutor.shutdown();

                if (!inspectorExecutor.awaitTermination(interval, TimeUnit.MILLISECONDS)) {
                    logger.warn("Inspector did not terminate within the maximum allowed time");
                    inspectorExecutor.shutdownNow();
                    if (!inspectorExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        logger.warn("Inspector did not terminate cleanly");
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to stop the inspector in a clean way: {}", e.getMessage(), e);
            }
            finally {
                inspectorContainer.stop();
            }
        }
        else {
            logger.warn("Ignoring a stop request for the inspector because it is already stopped");
        }
    }

    @Override
    public void handle(final TestFailedNotification note) {
        super.handle(note);

        if (inspectorExecutor != null) {
            logger.debug("Stopping the inspection as a result of a test failure notification by one of the peers");
            doInspectorStop();
        }
    }


    @Override
    public void handle(final TestSuccessfulNotification note) {
        super.handle(note);

        if (inspectorExecutor != null) {
            logger.debug("Stopping the inspection as a result of a test success notification by one of the peers");
            doInspectorStop();
        }
    }

    @Override
    public void handle(final LogRequest note) {
        super.handle(note, logDir, getPeerInfo());
    }

    @Override
    public void handle(final RoleAssign note) {
        getClient().replyOk(note);
    }

    @Override
    public void handle(final RoleUnassign note) {
        getClient().replyOk(note);
    }

    @Override
    public void handle(final StartWorker note) {
        // NO-OP for now
    }

    @Override
    public void handle(final StopWorker note) {
        // NO-OP for now
    }

    @Override
    public void handle(StartTestRequest note) {
        super.handle(note);

        getClient().notifyStarted(getCurrentTest(), "");
    }
}

