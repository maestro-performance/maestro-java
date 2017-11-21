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

package net.orpiske.mpt.maestro.worker.jms;

import net.orpiske.mpt.common.content.ContentStrategy;
import net.orpiske.mpt.common.content.ContentStrategyFactory;
import net.orpiske.mpt.common.duration.TestDuration;
import net.orpiske.mpt.common.duration.TestDurationBuilder;
import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.WorkerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class JMSSenderWorker implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(JMSSenderWorker.class);
    private ContentStrategy contentStrategy;
    private TestDuration duration;
    private final AtomicLong messageCount = new AtomicLong(0);
    private volatile long startedEpochMillis = Long.MIN_VALUE;
    private String url;
    private long rate = 0;

    private boolean running = false;

    @Override
    public long startedEpochMillis() {
        return this.startedEpochMillis;
    }

    @Override
    public long messageCount() {
        return messageCount.get();
    }

    private void setMessageSize(String messageSize) {
        contentStrategy = ContentStrategyFactory.parse(messageSize);
    }

    private void setRate(String rate) {
        this.rate = Long.parseLong(rate);
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
    public void setWorkerOptions(WorkerOptions workerOptions) {
        setRate(workerOptions.getRate());
        setDuration(workerOptions.getDuration());
        setBroker(workerOptions.getBrokerURL());
        setMessageSize(workerOptions.getMessageSize());
    }

    /**
     * @return the expected start time of this operation
     */
    private static long waitUsingRate(final long startedTimeInNanos, final long count, final long intervalInNanos) throws InterruptedException {
        final long now = System.nanoTime();
        final long expectedTriggerTime = startedTimeInNanos + (count * intervalInNanos);
        final long waitNanos = expectedTriggerTime - now;
        if (waitNanos > 0) {
            //TODO warns if waitNanos is below the precision offered by the OS
            TimeUnit.NANOSECONDS.sleep(waitNanos);
        }
        //oops: too late!
        return expectedTriggerTime;
    }

    public void start() {
        running = true;
        startedEpochMillis = System.currentTimeMillis();
        logger.info("Starting the test");

        try {
            JMSSenderClient client;

            client = new JMSSenderClient();

            client.setUrl(url);
            client.setContentStrategy(contentStrategy);
            client.start();

            final long startedTimeInNanos = System.nanoTime();
            long count = 0;
            final long intervalInNanos = this.rate > 0 ? 1_000_000_000L / rate : 0;
            if (logger.isDebugEnabled()) {
                logger.debug("JMS Sender [" + Thread.currentThread().getId() + "] - has started firing events with interval= " + intervalInNanos + " ns [" + rate + " msg/sec]");
            }
            while (duration.canContinue(this) && isRunning()) {
                if (intervalInNanos > 0) {
                    //TODO the expected start time could be used to be sent instead of the real one to measure
                    //without coordinated omission
                    waitUsingRate(startedTimeInNanos, count, intervalInNanos);
                }
                client.sendMessages();
                count++;
                //update message sent count
                this.messageCount.lazySet(count);
            }
        } catch (InterruptedException e) {
            logger.error("JMS Sender [" + Thread.currentThread().getId() + "] interrupted while sending messages: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unable to start the worker: {}", e.getMessage(), e);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {
        running = false;
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
