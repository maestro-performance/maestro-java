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

package org.maestro.worker.tests.support.runner;

import org.apache.commons.io.FileUtils;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.common.Role;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.worker.common.executor.MaestroWorkerExecutor;

import java.io.File;
import java.net.URL;

/**
 * A small, but complete, maestro peer to be used for testing
 */
@SuppressWarnings("FieldCanBeLocal")
public class MiniPeer {
    private static final String MINIPEERS_TOPIC =  "/mpt/minipeer";
    public static final String SENDER_TOPIC =  MINIPEERS_TOPIC + "/sender";
    public static final String RECEIVER_TOPIC =  MINIPEERS_TOPIC + "/receiver";


    private MaestroWorkerExecutor executor;

    private final String worker;
    private final String maestroUrl;
    private final String role;
    private final String host;


    public MiniPeer(String worker, String maestroUrl, String role, String host) {
        this.worker = worker;
        this.maestroUrl = maestroUrl;
        this.role = role;
        this.host = host;
    }

    public void start() throws Exception {
        @SuppressWarnings("unchecked")
        Class<MaestroWorker> clazz = (Class<MaestroWorker>) Class.forName(worker);

        URL url = this.getClass().getResource("/");

        String logPath;
        File logDir;
        if (url == null) {
            logPath = FileUtils.getTempDirectoryPath() + File.separator + role;
        }
        else {
            logPath = url.getPath() + File.separator + role;
        }
        logDir = new File(logPath);

        final PeerInfo peerInfo = new WorkerPeer("test", "localhost");

        executor = new MaestroWorkerExecutor(maestroUrl, peerInfo, logDir, null);

        if (role.equals("sender")) {
            String[] topics = {MaestroTopics.WORKERS_TOPIC, MaestroTopics.NOTIFICATION_TOPIC, SENDER_TOPIC};

            peerInfo.setRole(Role.SENDER);
            executor.start(topics);
        }
        else {
            String[] topics = {MaestroTopics.WORKERS_TOPIC, MaestroTopics.NOTIFICATION_TOPIC, RECEIVER_TOPIC};

            peerInfo.setRole(Role.RECEIVER);
            executor.start(topics);
        }

        Thread thread = new Thread(executor);
        thread.start();
    }


    public void stop() {
        if (executor != null) {
            executor.stop();
        }
    }
}
