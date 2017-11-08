package net.orpiske.mpt.maestro.worker.main;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.client.AbstractMaestroExecutor;
import net.orpiske.mpt.maestro.client.MaestroTopics;
import net.orpiske.mpt.maestro.worker.base.MaestroWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaestroWorkerExecutor extends AbstractMaestroExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerExecutor.class);

    public MaestroWorkerExecutor(final String url) throws MaestroConnectionException {
        super(new MaestroWorkerManager(url, "default"));

        // logger.debug("Subscribing the maestro topics {}", MaestroTopics.MAESTRO_TOPIC);
        getMaestroPeer().subscribe(MaestroTopics.MAESTRO_SENDER_TOPICS);
    }
}
