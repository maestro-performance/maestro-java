package org.maestro.worker.main;

import org.maestro.common.exceptions.MaestroException;
import org.maestro.common.worker.MaestroWorker;
import org.maestro.client.exchange.AbstractMaestroExecutor;
import org.maestro.worker.base.MaestroWorkerManager;
import org.maestro.worker.base.TestWorkerManager;
import org.maestro.worker.ds.MaestroDataServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MaestroWorkerExecutor extends AbstractMaestroExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerExecutor.class);
    private final Thread dataServerThread;
    private MaestroDataServer dataServer;

    public MaestroWorkerExecutor(final String url, final String role, final String host, final File logDir, final Class<MaestroWorker> workerClass) throws MaestroException {
        super(new TestWorkerManager(url, role, host, logDir, workerClass));

        logger.info("Creating the data server");
        dataServer = new MaestroDataServer(logDir);

        dataServerThread = new Thread(dataServer);
        dataServerThread.start();
    }
}
