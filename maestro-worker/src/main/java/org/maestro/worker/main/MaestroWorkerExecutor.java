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

package org.maestro.worker.main;

import org.maestro.client.exchange.AbstractMaestroPeer;
import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.client.exchange.AbstractMaestroExecutor;
import org.maestro.worker.base.ConcurrentWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MaestroWorkerExecutor extends AbstractMaestroExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerExecutor.class);
    private Thread dataServerThread;


    public MaestroWorkerExecutor(final AbstractMaestroPeer maestroPeer, final MaestroDataServer dataServer) {
        super(maestroPeer);

        initDataServer(dataServer);
    }

    public MaestroWorkerExecutor(final String url, final String role, final String host, final File logDir,
                                 final Class<MaestroWorker> workerClass, final MaestroDataServer dataServer) throws MaestroException {
        super(new ConcurrentWorkerManager(url, role, host, logDir, workerClass, dataServer));

        initDataServer(dataServer);
    }

    private void initDataServer(MaestroDataServer dataServer) {
        logger.info("Creating the data server");

        dataServerThread = new Thread(dataServer);
        dataServerThread.start();
    }
}
