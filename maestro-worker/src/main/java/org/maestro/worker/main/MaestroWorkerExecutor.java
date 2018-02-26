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
