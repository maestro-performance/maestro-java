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

import net.orpiske.mpt.common.duration.TestDuration;
import net.orpiske.mpt.common.duration.TestDurationBuilder;
import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.MessageInfo;
import net.orpiske.mpt.common.worker.WorkerSnapshot;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class JMSReceiverWorker implements MaestroReceiverWorker {
    private static final Logger logger = LoggerFactory.getLogger(JMSReceiverWorker.class);

    private BlockingQueue<WorkerSnapshot> queue;
    private TestDuration duration;

    private String url;

    public void setFCL(String fcl) {

    }

    public RateWriter getRateWriter() {
        return null;
    }

    public void setRateWriter(RateWriter rateWriter) {

    }

    public void setLatencyWriter(LatencyWriter latencyWriter) {

    }

    public LatencyWriter getLatencyWriter() {
        return null;
    }

    public void setBroker(String url) {

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
        // NO-OP
    }

    public void setThrottle(String value) {
        // NO-OP
    }

    public void setRate(String rate) {
        try {
            JMSReceiverClient client = new JMSReceiverClient();

            client = new JMSReceiverClient();

            client.setUrl(url);

            client.start();

            Instant startTime = Instant.now();

            long count = 0;

            WorkerSnapshot snapshot = new WorkerSnapshot();

            snapshot.setStartTime(startTime);


            while (duration.canContinue(snapshot)) {
                snapshot.setCount(count);

                Instant now = Instant.now();
                snapshot.setNow(now);

                count++;
                MessageInfo info = client.receiveMessages();



            }
        } catch (Exception e) {

        }
    }

    public void start() {

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
