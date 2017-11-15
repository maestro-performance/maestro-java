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
import net.orpiske.mpt.common.content.FixedSizeContent;
import net.orpiske.mpt.common.content.VariableSizeContent;
import net.orpiske.mpt.common.duration.TestDuration;
import net.orpiske.mpt.common.duration.TestDurationBuilder;
import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.ThroughputStats;
import net.orpiske.mpt.common.worker.WorkerSnapshot;
import net.orpiske.mpt.common.writers.RateWriter;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;


public class JMSSenderWorker implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(JMSSenderWorker.class);

    private ContentStrategy contentStrategy;
    private RateWriter rateWriter;
    private TestDuration duration;
    private BlockingQueue<WorkerSnapshot> queue;

    private String url;
    private int messageSize;
    private long rate;

    public RateWriter getRateWriter() {
        return rateWriter;
    }

    public void setRateWriter(RateWriter rateWriter) {
        this.rateWriter = rateWriter;
    }

    public void setBroker(String url) {
        this.url = url;
    }

    public void setDuration(String duration) {
        try {
            this.duration = TestDurationBuilder.build(duration);
        } catch (DurationParseException e) {
            e.printStackTrace();
        }
    }

    public void setLogLevel(String logLevel) {

    }

    public void setParallelCount(String parallelCount) {

    }

    public void setMessageSize(String messageSize) {
        if (messageSize.contains("~")) {
            this.messageSize = Integer.parseInt(messageSize.replace("~", ""));

            contentStrategy = new VariableSizeContent();
        }
        else {
            this.messageSize = Integer.parseInt(messageSize);

            contentStrategy = new FixedSizeContent();
        }

        contentStrategy.setSize(this.messageSize);
    }

    public void setThrottle(String value) {

    }

    public void setRate(String rate) {
        this.rate = Long.parseLong(rate);
    }

    public void start() {
        int sampleInterval = 10;

        try {
            JMSSenderClient client;

            client = new JMSSenderClient();

            client.setUrl(url);
            client.setContentStrategy(contentStrategy);

            client.start();

            Instant startTime = Instant.now();

            long count = 0;
            long lastCount = 0;

            WorkerSnapshot snapshot = new WorkerSnapshot();

            long interval = 1000000 / rate;

            snapshot.setStartTime(startTime);

            Instant last = startTime;

            while (duration.canContinue(snapshot)) {
                snapshot.setCount(count);

                Instant now = Instant.now();
                snapshot.setNow(now);

                Instant eta = now.plusNanos(interval * 1000);
                snapshot.setEta(eta);

                client.sendMessages();
                count++;

                long elapsedSecs = now.getEpochSecond() - last.getEpochSecond();
                if (elapsedSecs >= sampleInterval) {
                    long processedCount = count - lastCount;

                    ThroughputStats tp = new ThroughputStats();

                    tp.setCount(processedCount);
                    tp.setDuration(Duration.ofMillis(now.toEpochMilli() - last.toEpochMilli()));


                }

                queue.add(SerializationUtils.clone(snapshot));

                if (eta.isBefore(now)) {
                    Thread.sleep(now.minusNanos(eta.getNano()).getNano());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }

    public void halt() {

    }

    public WorkerSnapshot stats() {
        return null;
    }

    @Override
    public void setQueue(BlockingQueue<WorkerSnapshot> queue) {
        this.queue = queue;
    }
}
