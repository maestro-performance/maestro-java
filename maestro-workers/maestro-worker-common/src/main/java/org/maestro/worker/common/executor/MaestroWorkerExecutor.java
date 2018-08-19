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

package org.maestro.worker.common.executor;

import org.maestro.client.exchange.AbstractMaestroExecutor;
import org.maestro.client.exchange.AbstractMaestroPeer;
import org.maestro.client.exchange.support.PeerInfo;
import org.maestro.client.notes.MaestroEvent;
import org.maestro.client.notes.MaestroEventListener;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.worker.common.ConcurrentWorkerManager;
import org.maestro.worker.common.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MaestroWorkerExecutor extends AbstractMaestroExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerExecutor.class);


    public MaestroWorkerExecutor(final AbstractMaestroPeer<MaestroEvent<MaestroEventListener>> maestroPeer, final MaestroDataServer dataServer) {
        super(maestroPeer);

        initDataServer(dataServer);
    }

    public MaestroWorkerExecutor(final String url, final PeerInfo peerInfo, final File logDir, final MaestroDataServer dataServer) throws MaestroException {
        super(new ConcurrentWorkerManager(url, peerInfo, logDir, dataServer));

        initDataServer(dataServer);
    }

    private void initDataServer(MaestroDataServer dataServer) {
        logger.info("Creating the data server");

        Thread dataServerThread = new Thread(dataServer);
        dataServerThread.start();
    }
}
