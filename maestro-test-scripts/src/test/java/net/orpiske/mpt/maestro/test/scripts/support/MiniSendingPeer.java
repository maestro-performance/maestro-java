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

package net.orpiske.mpt.maestro.test.scripts.support;

import net.orpiske.mpt.common.worker.MaestroWorker;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.worker.main.MaestroWorkerExecutor;

import java.io.File;

public class MiniSendingPeer {
    Thread thread;
    MaestroWorkerExecutor executor;


    public void start() throws Exception {
        Class<MaestroWorker> clazz = (Class<MaestroWorker>) Class.forName("net.orpiske.mpt.maestro.worker.jms.JMSSenderWorker");

        executor = new MaestroWorkerExecutor("mqtt://localhost:1883", "sender", "localhost",
                new File(getClass().getResource(".").getFile()), clazz);

        executor.start(MaestroTopics.MAESTRO_SENDER_TOPICS);

        thread = new Thread(executor);
        thread.start();
    }

    public void stop() {
        executor.stop();
    }
}
