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

package org.maestro.worker.jms;

import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.jms.ReceiverClient;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.common.worker.WorkerStateInfo;
import org.maestro.common.writers.OneToOneWorkerChannel;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;


/**
 * A sender worker for JMS-based testing
 */
public class JMSReceiverWorker implements MaestroReceiverWorker {

    private static final Logger logger = LoggerFactory.getLogger(JMSReceiverWorker.class);

    private TestDuration duration;
    private final AtomicLong messageCount = new AtomicLong(0);
    private volatile long startedEpochMillis = Long.MIN_VALUE;
    //TODO it could be injected by outside because the precision could be improved using ad-hoc clock timers
    private static final long HIGHEST_TRACKABLE_VALUE = TimeUnit.HOURS.toMicros(1);
    private final SingleWriterRecorder latencyRecorder = new SingleWriterRecorder(HIGHEST_TRACKABLE_VALUE, 3);
    //TODO the size need to be configured
    private final OneToOneWorkerChannel workerChannel = new OneToOneWorkerChannel(128 * 1024);

    private final WorkerStateInfo workerStateInfo = new WorkerStateInfo();

    private String url;
    private final Supplier<? extends ReceiverClient> clientFactory;
    private int number;

    @Override
    public OneToOneWorkerChannel workerChannel() {
        return workerChannel;
    }

    @Override
    public long messageCount() {
        return messageCount.get();
    }

    @Override
    public WorkerStateInfo getWorkerState() {
        return workerStateInfo;
    }


    @Override
    public long startedEpochMillis() {
        return startedEpochMillis;
    }

    private void setFCL(String fcl) {

    }

    private void setBroker(String url) {
        this.url = url;
    }

    private void setDuration(String duration) {
        try {
            this.duration = TestDurationBuilder.build(duration);
        } catch (DurationParseException e) {
            e.printStackTrace();
        }
    }

    public JMSReceiverWorker() {
        this(JMSReceiverClient::new);
    }

    public JMSReceiverWorker(Supplier<? extends ReceiverClient> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void setWorkerNumber(int number) {
        this.number = number;
    }

    @Override
    public void setWorkerOptions(WorkerOptions workerOptions) {
        setBroker(workerOptions.getBrokerURL());
        setDuration(workerOptions.getDuration());
    }

    private static void handleNegativeSampleError(final long sendTimeEpochMicros, final long nowInMicros) {
        logger.error("Dropped sample: SendTimeEpochMicros {} > ReceivedTimeEpochMicros {}",
                sendTimeEpochMicros, nowInMicros);
    }

    private static void handleHugeSampleError(final long sendTimeEpochMicros, final long nowInMicros) {
        logger.error("Normalized sample: (ReceivedTimeEpochMicros {} - SendTimeEpochMicros {}) > " +
                        HIGHEST_TRACKABLE_VALUE,
                nowInMicros, sendTimeEpochMicros);
    }

    public void start() {
        startedEpochMillis = System.currentTimeMillis();
        logger.info("Starting the test");

        final ReceiverClient client = clientFactory.get();
        try {
            final EpochMicroClock epochMicroClock = EpochClocks.exclusiveMicro();

            client.setUrl(url);

            workerStateInfo.setState(true, null, null);
            client.setNumber(number);
            client.start();

            long count = 0;

            while (duration.canContinue(this) && isRunning()) {
                final long sendTimeEpochMicros = client.receiveMessages();
                if (sendTimeEpochMicros != ReceiverClient.noMessagePayload()) {
                    final long nowInMicros = epochMicroClock.microTime();
                    long elapsedMicros = nowInMicros - sendTimeEpochMicros;
                    boolean validLatency = true;
                    if (elapsedMicros < 0) {
                        handleNegativeSampleError(sendTimeEpochMicros, nowInMicros);
                        validLatency = false;
                    } else if (elapsedMicros == 0) {
                        logger.warn("Registered Latency of 0: please consider to improve timestamp precision");
                    } else if (elapsedMicros > HIGHEST_TRACKABLE_VALUE) {
                        handleHugeSampleError(sendTimeEpochMicros, nowInMicros);
                        //record it but avoid histogram to throw any error
                        elapsedMicros = HIGHEST_TRACKABLE_VALUE;
                    }
                    if (validLatency) {
                        //we can perform fnc check here to fail the test
                        //TODO use Histogram:recordValueWithExpectedInterval if the rate is known
                        latencyRecorder.recordValue(elapsedMicros);
                    }
                    //record everything
                    workerChannel.emitRate(sendTimeEpochMicros, nowInMicros);
                    count++;
                    messageCount.lazySet(count);
                }
            }

            logger.info("Test completed successfully");
            workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS, null);
        } catch (Exception e) {
            logger.error("Unable to start the receiver worker: {}", e.getMessage(), e);

            workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, e);
        } finally {
            //the test could be considered already stopped here, but cleaning up JMS resources could take some time anyway
            client.stop();
        }
    }

    @Override
    public Histogram takeLatenciesSnapshot(Histogram intervalHistogram) {
        return latencyRecorder.getIntervalHistogram(intervalHistogram);
    }

    @Override
    public boolean isRunning() {
        return workerStateInfo.isRunning();
    }

    @Override
    public void stop() {
        workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED, null);
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