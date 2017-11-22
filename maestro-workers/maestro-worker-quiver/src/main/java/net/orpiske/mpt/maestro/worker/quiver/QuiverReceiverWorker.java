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

package net.orpiske.mpt.maestro.worker.quiver;

import net.orpiske.mpt.common.ConsoleHijacker;
import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.common.worker.WorkerSnapshot;
import net.orpiske.mpt.common.worker.WorkerStateInfo;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;
import net.ssorj.quiver.QuiverArrowJms;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class QuiverReceiverWorker implements MaestroReceiverWorker {
    private static final Logger logger = LoggerFactory.getLogger(QuiverReceiverWorker.class);

    private RateWriter rateWriter;
    private LatencyWriter latencyWriter;

    private String brokerUrl;
    private String duration;
    private String messageSize;

    private WorkerStateInfo workerStateInfo = new WorkerStateInfo();

    @Override
    public WorkerStateInfo getWorkerState() {
        return workerStateInfo;
    }

    @Override
    public RateWriter getRateWriter() {
        return rateWriter;
    }

    @Override
    public void setRateWriter(RateWriter rateWriter) {
        this.rateWriter = rateWriter;
    }

    @Override
    public void setLatencyWriter(LatencyWriter latencyWriter) {
        this.latencyWriter = latencyWriter;
    }

    @Override
    public LatencyWriter getLatencyWriter() {
        return latencyWriter;
    }

    private String getCreationTime(String line) {
        String[] lineParts = line.split(",");

        return lineParts[1];
    }

    private String getArrivalTime(String line) {
        String[] lineParts = line.split(",");

        return lineParts[2];
    }

    private void setBroker(String url) {
        this.brokerUrl = url;
    }

    private void setDuration(String duration) {
        if (duration.matches("[a-zA-Z]")) {
            // TODO: decide what to do here.
        }
        else {
            this.duration = duration;
        }
    }

    private void setMessageSize(String messageSize) {
        if (messageSize.contains("~")) {
            logger.warn("Variable message sizes are not supported on this worker");
            this.messageSize = messageSize.replace("~", "");
        }
        else {
            this.messageSize = messageSize;
        }
    }


    @Override
    public void setWorkerOptions(WorkerOptions workerOptions) {
        setBroker(workerOptions.getBrokerURL());
        setDuration(workerOptions.getDuration());
    }

    @Override
    public void start() {
        logger.info("Starting the receiver worker");

        try {
            String[] args = QuiverArgumentBuilder.buildArguments("receive", brokerUrl, duration, messageSize);

            ConsoleHijacker ch = ConsoleHijacker.getInstance();

            workerStateInfo.setRunning(true);
            ch.start();

            QuiverArrowJms.doMain(args);

            String data = ch.stop();

            rateWriter.setConverter(new QuiverRateConverter());

            String[] lines = StringUtils.split(data,"\n");

            for (String line : lines) {
                String createdTime = getCreationTime(line);
                String arrivedTime = getArrivalTime(line);

                long latency = Long.parseLong(arrivedTime) - Long.parseLong(createdTime);

                latencyWriter.writeLine(latency);
                rateWriter.writeLine(createdTime, arrivedTime);
            }

            rateWriter.close();
            latencyWriter.close();

            workerStateInfo.setException(null);
            workerStateInfo.setExitStatus(WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS);
        } catch (Exception e) {
            logger.error("Unable to start the receiver worker: {}", e.getMessage(), e);

            workerStateInfo.setRunning(false);
            workerStateInfo.setException(e);
            workerStateInfo.setExitStatus(WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE);
        }
        finally {
            workerStateInfo.setRunning(false);
        }
    }

    @Override
    public boolean isRunning() {
        return workerStateInfo.isRunning();
    }

    @Override
    public void stop() {
        workerStateInfo.setRunning(false);
        workerStateInfo.setExitStatus(WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED);

        Thread.currentThread().stop();
    }

    @Override
    public void halt() {
        stop();
    }

    @Override
    public WorkerSnapshot stats() {
        return null;
    }

    @Override
    public void setQueue(BlockingQueue<WorkerSnapshot> queue) {
        // NO-OP ... unnecessary for this worker type
    }

    @Override
    public void run() {
        start();
    }
}
