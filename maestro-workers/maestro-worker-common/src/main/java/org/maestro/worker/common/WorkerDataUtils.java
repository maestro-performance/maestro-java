/*
 * Copyright 2018 Otavio Rodolfo Piske
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

class WorkerDataUtils {
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
