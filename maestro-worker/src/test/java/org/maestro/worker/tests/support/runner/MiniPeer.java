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

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.maestro.client.exchange.ConsumerEndpoint;
import org.maestro.client.exchange.MaestroTopics;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.exchange.support.WorkerPeer;
import org.maestro.client.resolver.MaestroClientResolver;
import org.maestro.common.Role;
import org.maestro.common.client.MaestroClient;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.worker.common.executor.MaestroWorkerExecutor;

/**
 * A small, but complete, maestro peer to be used for testing
 */
@SuppressWarnings("FieldCanBeLocal")
public class MiniPeer {
    private MaestroWorkerExecutor executor;

    private final String worker;
    private final String maestroUrl;
    private final String role;
    private final String host;

    private static final PeerInfo SENDER_INFO = new SenderInfo();
    private static final PeerInfo RECEIVER_INFO = new ReceiverInfo();

    private static String[] senderTopics(final String id) {
        return new String[] {
                MaestroTopics.PEER_TOPIC,
                MaestroTopics.WORKERS_TOPIC,
                MaestroTopics.NOTIFICATION_TOPIC,
                MaestroTopics.MAESTRO_LOGS_TOPIC,
                MaestroTopics.peerTopic(Role.SENDER),
                MaestroTopics.peerTopic(id),
                MaestroTopics.peerTopic(SENDER_INFO)
        };
    }

    private static String[] receiverTopics(final String id) {
        return new String[] {
                MaestroTopics.PEER_TOPIC,
                MaestroTopics.WORKERS_TOPIC,
                MaestroTopics.NOTIFICATION_TOPIC,
                MaestroTopics.MAESTRO_LOGS_TOPIC,
                MaestroTopics.peerTopic(Role.RECEIVER),
                MaestroTopics.peerTopic(id),
                MaestroTopics.peerTopic(RECEIVER_INFO),
        };
    }


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

        MaestroClient client = MaestroClientResolver.newClient(maestroUrl);

        String[] topics;
        if (role.equals("sender")) {
            topics = senderTopics("123");

            peerInfo.setRole(Role.SENDER);
        }
        else {
            topics = receiverTopics("456");

            peerInfo.setRole(Role.RECEIVER);
        }

        ConsumerEndpoint<MaestroNote> consumerEndpoint = MaestroClientResolver
                .newConsumerEndpoint(maestroUrl, topics);

        executor = new MaestroWorkerExecutor(client, consumerEndpoint, peerInfo, logDir);
        executor.start(topics);

        Thread thread = new Thread(executor);
        thread.start();
    }


    public void stop() {
        if (executor != null) {
            executor.stop();
        }
    }
}
