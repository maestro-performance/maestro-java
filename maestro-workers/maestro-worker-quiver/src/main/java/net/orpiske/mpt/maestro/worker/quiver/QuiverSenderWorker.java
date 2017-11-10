/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.maestro.worker.quiver;

import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.Stats;
import net.ssorj.quiver.QuiverArrowJms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class QuiverSenderWorker implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(QuiverSenderWorker.class);

    private String brokerUrl;
    private String duration;
    private boolean verbose = false;
    private String messageSize;

    @Override
    public void setBroker(String url) {
        this.brokerUrl = url;


    }

    @Override
    public void setDuration(String duration) {
        if (duration.matches("[a-zA-Z]")) {
            // TODO: decide what to do here.
        }
        else {
            this.duration = duration;
        }
    }

    @Override
    public void setLogLevel(String logLevel) {
        if (logLevel.equals("debug") || logLevel.equals("DEBUG")) {
            this.verbose = true;
        }
    }

    @Override
    public void setParallelCount(String parallelCount) {
        // TODO: unsupported ... what to do?
        logger.warn("Concurrent connections are not supported on this worker");
    }

    @Override
    public void setMessageSize(String messageSize) {
        if (messageSize.contains("~")) {
            this.messageSize = messageSize.replace("~", "");

            logger.warn("Variable message sizes are not supported on this worker. Set to: {}",
                    this.messageSize);
        }
        else {
            this.messageSize = messageSize;
        }
    }

    @Override
    public void setThrottle(String value) {
        logger.warn("Concurrent connections are not supported on this worker");
    }

    @Override
    public void setRate(String rate) {
        logger.warn("Target rate is not supported on this worker");
    }

    @Override
    public void setFCL(String fcl) {
        logger.warn("Fail-condition-on-latency is not supported on this worker");
    }

    @Override
    public void start() {
        logger.info("Starting the worker");

        URI uri = null;
        try {
            uri = new URI(brokerUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();

            return;
        }

        System.setProperty("arrow.jms.url", brokerUrl);
        System.setProperty("java.naming.factory.initial", "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        String port = (uri.getPort() > 0 ? Integer.toString(uri.getPort()) : "-");

        String[] args = {
                "client",
                "active",
                "send",
                "maestro",
                uri.getHost(),
                port,
                uri.getPath().substring(1),
                duration,
                messageSize,
                "1024",
                "0",
                ""};

        try {
            QuiverArrowJms.doMain(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void halt() {

    }

    @Override
    public Stats stats() {
        return null;
    }

}
