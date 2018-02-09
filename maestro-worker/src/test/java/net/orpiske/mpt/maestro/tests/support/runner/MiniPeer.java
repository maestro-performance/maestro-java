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

package net.orpiske.mpt.maestro.tests.support.runner;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.worker.main.MaestroWorkerExecutor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

/**
 * A small, but complete, maestro peer to be used for testing
 */
public class MiniPeer {
    private MaestroWorkerExecutor executor;

    private String worker;
    private String maestroUrl;
    private String role;
    private String host;


    public MiniPeer(String worker, String maestroUrl, String role, String host) {
        this.worker = worker;
        this.maestroUrl = maestroUrl;
        this.role = role;
        this.host = host;
    }

    /**
     * Starts the peer
     *
     * @throws Exception
     */
    public void start() throws Exception {
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


        executor = new MaestroWorkerExecutor(maestroUrl, role, host, logDir, clazz);

        if (role.equals("sender")) {
            executor.start(MaestroTopics.MAESTRO_SENDER_TOPICS);
        }
        else {
            executor.start(MaestroTopics.MAESTRO_RECEIVER_TOPICS);
        }

        Thread thread = new Thread(executor);
        thread.start();
    }


    /**
     * Stops the peer
     */
    public void stop() {
        if (executor != null) {
            executor.stop();
        }
    }
}
