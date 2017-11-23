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
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.common.worker.WorkerStateInfo;
import net.ssorj.quiver.QuiverArrowJms;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class QuiverSenderWorker implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(QuiverSenderWorker.class);

    private volatile long startedEpochMillis = Long.MIN_VALUE;

    private String brokerUrl;
    private String duration;
    private String messageSize;

    private WorkerStateInfo workerStateInfo = new WorkerStateInfo();

    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }


    @Override
    public long messageCount() {
        return 0;
    }


    @Override
    public WorkerStateInfo getWorkerState() {
        return workerStateInfo;
    }

    private String getEta(String line) {
        String[] lineParts = line.split(",");

        return lineParts[1];
    }

    private String getAta(String line) {
        String[] lineParts = line.split(",");

        return lineParts[1];
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
            this.messageSize = messageSize.replace("~", "");

            logger.warn("Variable message sizes are not supported on this worker. Set to: {}",
                    this.messageSize);
        }
        else {
            this.messageSize = messageSize;
        }
    }

    @Override
    public void setWorkerOptions(WorkerOptions workerOptions) {
        setDuration(workerOptions.getDuration());
        setBroker(workerOptions.getBrokerURL());
        setMessageSize(workerOptions.getMessageSize());
    }

    @Override
    public void start() {
        logger.info("Starting the sender worker");
        startedEpochMillis = System.currentTimeMillis();

        try {
            String[] args = QuiverArgumentBuilder.buildArguments("send", brokerUrl, duration, messageSize);

            ConsoleHijacker ch = ConsoleHijacker.getInstance();

            workerStateInfo.setState(true, null, null);
            ch.start();

            QuiverArrowJms.doMain(args);

            String data = ch.stop();

            //rateWriter.setConverter(new QuiverRateConverter());

            String[] lines = StringUtils.split(data,"\n");

            //for (String line : lines) {
                //rateWriter.writeLine(getEta(line), getAta(line));
            //}

            //rateWriter.close();
            workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS, null);
        } catch (Exception e) {
            logger.error("Unable to start the sender worker: {}", e.getMessage(), e);

            workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, e);
        }
    }

    @Override
    public boolean isRunning() {
        return workerStateInfo.isRunning();
    }

    @Override
    public void stop() {
        workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED, null);

        Thread.currentThread().stop();
    }

    @Override
    public void halt() {
        stop();
    }

    @Override
    public void run() {
        start();
    }
}
