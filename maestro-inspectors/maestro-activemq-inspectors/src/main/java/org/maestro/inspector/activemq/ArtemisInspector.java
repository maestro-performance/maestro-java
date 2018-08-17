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

package org.maestro.inspector.activemq;

import org.apache.commons.configuration.AbstractConfiguration;
import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.client.MaestroReceiver;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.inspector.MaestroInspector;
import org.maestro.common.inspector.types.*;
import org.maestro.common.test.InspectorProperties;
import org.maestro.common.worker.TestLogUtils;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.inspector.activemq.writers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A inspector class for Apache ActiveMQ Artemis
 */
public class ArtemisInspector implements MaestroInspector {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisInspector.class);
    private long startedEpochMillis = Long.MIN_VALUE;
    private boolean running = false;
    private String url;
    private String user;
    private String password;
    private File baseLogDir;
    private TestDuration duration;
    private WorkerOptions workerOptions;
    private MaestroReceiver endpoint;

    private ArtemisDataReader artemisDataReader;

    private final int interval;

    public ArtemisInspector() {
        final AbstractConfiguration config = ConfigurationWrapper.getConfig();
        interval = config.getInteger("inspector.sleep.interval", 5000);
    }

    public void setUrl(final String value) {
        try {
            URL url = new URL(value);

            /*
             * Jolokia client does not handle well the userinfo part of the URL ... that's
             * why it reformats the URL without it.
             */
            this.url = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/" + url.getPath();

            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] parts = userInfo.split(":");
                String username = parts[0];
                this.setUser(username);

                if (parts.length == 2) {
                    this.setPassword(parts[1]);
                }
            }
        } catch (MalformedURLException e) {
            throw new MaestroException("Unable to set the management interface URL", e);
        }
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setWorkerOptions(final WorkerOptions workerOptions) throws DurationParseException {
        this.duration = TestDurationBuilder.build(workerOptions.getDuration());

        this.workerOptions = workerOptions;
    }

    @Override
    public void setBaseLogDir(final File baseLogDir) {
        this.baseLogDir = baseLogDir;
    }

    @Override
    public void setEndpoint(MaestroReceiver endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isRunning() {
        return running;
    }

    private void connect() {
        J4pClient j4p = J4pClient.url(url)
                .user(user)
                .password(password)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .build();

        artemisDataReader = new ArtemisDataReader(j4p);
    }


    public int start() throws Exception {
        File logDir = TestLogUtils.nextTestLogDir(this.baseLogDir);
        InspectorProperties inspectorProperties = new InspectorProperties();

        RuntimeInfoWriter runtimeInfoWriter = new RuntimeInfoWriter(inspectorProperties);
        OSInfoWriter osInfoWriter = new OSInfoWriter(inspectorProperties);
        ProductInfoWriter productInfoWriter = new ProductInfoWriter(inspectorProperties);

        try (JVMMemoryInfoWriter heapMemoryWriter = new JVMMemoryInfoWriter(logDir, "heap");
             JVMMemoryInfoWriter jvmMemoryAreasWriter = new JVMMemoryInfoWriter(logDir, "memory-areas");
             QueueInfoWriter queueInfoWriter = new QueueInfoWriter(logDir, "queues")
        )
        {
            startedEpochMillis = System.currentTimeMillis();
            running = true;

            if (url == null) {
                logger.error("No management interface was given for the test. Therefore, ignoring");
                return 1;
            }

            connect();

            writeInspectorProperties(logDir, inspectorProperties, runtimeInfoWriter, osInfoWriter, productInfoWriter);

            while (duration.canContinue(this) && isRunning()) {
                LocalDateTime now = LocalDateTime.now();
                heapMemoryWriter.write(now, artemisDataReader.jvmHeapMemory());

                List<JVMMemoryInfo> memoryInfoList = artemisDataReader.jvmMemoryAreas();
                for (JVMMemoryInfo memoryInfo : memoryInfoList) {
                    jvmMemoryAreasWriter.write(now, memoryInfo);
                }

                try {
                    QueueInfo queueInfoList = artemisDataReader.queueInformation();
                    queueInfoWriter.write(now, queueInfoList);
                }
                catch (Exception e) {
                    logger.error("Unable to read queue information: {}", e.getMessage(), e);
                }

                Thread.sleep(interval);
            }

            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            logger.debug("The test has finished and the Artemis inspector is terminating");

            return 0;
        } catch (InterruptedException eie) {
            TestLogUtils.createSymlinks(this.baseLogDir, false);
            endpoint.notifySuccess("Inspector finished successfully");
            return 0;
        }
        catch (Exception e) {
            TestLogUtils.createSymlinks(this.baseLogDir, true);
            endpoint.notifyFailure("Inspector failed");
            throw e;
        }
        finally {
            startedEpochMillis = Long.MIN_VALUE;
        }
    }

    private void writeInspectorProperties(File logDir, InspectorProperties inspectorProperties,
                                          RuntimeInfoWriter runtimeInfoWriter, OSInfoWriter osInfoWriter,
                                          ProductInfoWriter productInfoWriter) throws MalformedObjectNameException, J4pException, IOException {
        setCommonProperties(inspectorProperties, workerOptions);

        OSInfo osInfo = artemisDataReader.operatingSystem();
        osInfoWriter.write(null, osInfo);

        RuntimeInfo runtimeInfo = artemisDataReader.runtimeInformation();
        runtimeInfoWriter.write(null, runtimeInfo);

        ProductInfo productInfo = artemisDataReader.productInformation();
        productInfoWriter.write(null, productInfo);

        File propertiesFile = new File(logDir, InspectorProperties.FILENAME);
        inspectorProperties.write(propertiesFile);
    }

    public void stop() {
        running = false;
    }

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }
}
