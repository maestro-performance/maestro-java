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

import org.HdrHistogram.Histogram;
import org.HdrHistogram.SingleWriterRecorder;
import org.maestro.common.duration.EpochClocks;
import org.maestro.common.duration.EpochMicroClock;
import org.maestro.common.duration.TestDuration;
import org.maestro.common.duration.TestDurationBuilder;
import org.maestro.common.exceptions.DurationParseException;
import org.maestro.common.jms.ReceiverClient;
import org.maestro.common.worker.MaestroReceiverWorker;
import org.maestro.common.worker.WorkerOptions;
import org.maestro.common.worker.WorkerStateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.util.concurrent.CountDownLatch;
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

    private final WorkerStateInfo workerStateInfo = new WorkerStateInfo();

    private String url;
    private final Supplier<? extends ReceiverClient> clientFactory;
    private int number;
    private CountDownLatch startSignal;
    private CountDownLatch endSignal;

    public JMSReceiverWorker() {
        this(JMSReceiverClient::new);
    }

    private JMSReceiverWorker(Supplier<? extends ReceiverClient> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public synchronized void setupBarriers(final CountDownLatch startSignal, final CountDownLatch endSignal) {
        this.startSignal = startSignal;
        this.endSignal = endSignal;
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

    private static void handleInvalidLatency(long sendTimeEpochMicros, long nowInMicros, long elapsedMicros) {
        if (elapsedMicros == 0) {
            logger.warn("Registered Latency of 0: please consider to improve timestamp precision");
        }
        else {
            handleNegativeSampleError(sendTimeEpochMicros, nowInMicros);
        }
    }

    public void start() {
        startedEpochMillis = System.currentTimeMillis();
        logger.info("Starting the JMS receiver worker");

        final ReceiverClient client = clientFactory.get();
        final long id = Thread.currentThread().getId();
        try {
            logger.debug("JMS receiver worker {} is now running the client startup", id);
            doClientStartup(client);

            logger.debug("JMS receiver worker {} is signaling as started", id);
            startSignal.countDown();

            logger.debug("JMS receiver worker {} has started running the receive loop", id);
            runReceiveLoop(client);
            logger.debug("JMS receiver worker {} has completed running the load loop", id);
        } catch (InterruptedException e) {
            logger.error("JMS receiver worker {} interrupted while receiving messages: {}", id,
                    e.getMessage());

            stop();
        }
        catch (Exception e) {
            if (!workerStateInfo.isRunning()) {
                logger.error("Unable to start the receiver worker: {}", e.getMessage(), e);
            }
            else {
                logger.error("Unexpected error while running the receiver worker: {}", e.getMessage(), e);
            }

            workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, e);
        } finally {
            exitStateCheck(id);

            //the test could be considered already stopped here, but cleaning up JMS resources could take some time anyway
            client.stop();
            logger.info("Finalized worker {} after receiving {} messages", id, messageCount);
            endSignal.countDown();
        }
    }

    private void exitStateCheck(long id) {
        if (workerStateInfo.getExitStatus() != WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE) {
            logger.info("Worker {} completed running successfully with {} messages received", id,
                    messageCount);
            if (workerStateInfo.getExitStatus() != WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED) {
                workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_SUCCESS, null);
            }
        }
    }

    private void runReceiveLoop(final ReceiverClient client) throws Exception {
        final EpochMicroClock epochMicroClock = EpochClocks.exclusiveMicro();
        long count = 0;
        final JmsOptions opts = ((JMSClient) client).getOpts();
        final boolean isClientAck = isClientAcknowledge(opts);

        while (duration.canContinue(this) && isRunning()) {
            final long sendTimeEpochMicros = client.receiveMessages(isAcknowledge(count, opts, isClientAck));

            if (sendTimeEpochMicros != ReceiverClient.noMessagePayload()) {
                final long nowInMicros = epochMicroClock.microTime();
                long elapsedMicros = nowInMicros - sendTimeEpochMicros;

                if (elapsedMicros >= 0) {
                    if (elapsedMicros > HIGHEST_TRACKABLE_VALUE) {
                        handleHugeSampleError(sendTimeEpochMicros, nowInMicros);
                        elapsedMicros = HIGHEST_TRACKABLE_VALUE;
                    }

                    latencyRecorder.recordValue(elapsedMicros);
                }
                else {
                    handleInvalidLatency(sendTimeEpochMicros, nowInMicros, elapsedMicros);
                }

                count++;
                messageCount.lazySet(count);
            }
        }
    }

    /**
     * return sessionMode number according to acknowledge type (TRANSACTED/CLIENTS_ACK)
     *
     * @param count current count of messages
     * @param opts Jms Options
     * @param isClientAck is acknowledge enabled
     * @return number of session mode, else -1
     */
    private int isAcknowledge(long count, JmsOptions opts, boolean isClientAck) {
        return isClientAck && count % opts.getBatchAcknowledge() == 0 ? opts.getSessionMode() : -1;
    }

    private void doClientStartup(final ReceiverClient client) throws Exception {
        client.setUrl(url);
        client.setNumber(number);
        client.start();

        workerStateInfo.setState(true, null, null);
    }

    private boolean isClientAcknowledge(JmsOptions opts) {
        return (opts.getSessionMode() == Session.CLIENT_ACKNOWLEDGE || opts.getSessionMode() == Session.SESSION_TRANSACTED)
                && opts.getBatchAcknowledge() > 0;
    }

    @Override
    public Histogram takeLatenciesSnapshot(Histogram intervalHistogram) {
        return latencyRecorder.getIntervalHistogram(intervalHistogram);
    }

    @Override
    public boolean isRunning() {
        return workerStateInfo.isRunning() && !Thread.currentThread().isInterrupted();
    }

    @Override
    public void stop() {
        workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_STOPPED, null);
    }

    @Override
    public void fail(Exception exception) {
        workerStateInfo.setState(false, WorkerStateInfo.WorkerExitStatus.WORKER_EXIT_FAILURE, exception);
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