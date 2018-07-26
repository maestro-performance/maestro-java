package org.maestro.worker.common;

import org.maestro.common.io.data.common.FileHeader;
import org.maestro.common.io.data.writers.BinaryRateWriter;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.MaestroSenderWorker;
import org.maestro.common.worker.MaestroWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class WorkerDataUtils {
    private static final Logger logger = LoggerFactory.getLogger(WorkerDataUtils.class);

    public static <T extends MaestroWorker> BinaryRateWriter writer(final File reportFolder, final T worker) throws IOException {
        assert worker != null : "Invalid worker type";

        if (worker instanceof MaestroSenderWorker) {
            return new BinaryRateWriter(new File(reportFolder, "sender.dat"),
                    FileHeader.WRITER_DEFAULT_SENDER);
        }
        if (worker instanceof MaestroReceiverWorker) {
            return new BinaryRateWriter(new File(reportFolder, "receiver.dat"),
                    FileHeader.WRITER_DEFAULT_SENDER);
        }

        logger.error("Invalid worker class: {}", worker.getClass());
        return null;
    }
}
