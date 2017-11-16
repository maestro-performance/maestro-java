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
import net.orpiske.mpt.common.worker.WorkerOptions;
import net.orpiske.mpt.common.worker.WorkerSnapshot;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class JMSReceiverWorker implements MaestroReceiverWorker,Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JMSReceiverWorker.class);

    private BlockingQueue<WorkerSnapshot> queue;
    private TestDuration duration;

    private String url;
    private boolean running = false;

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



    @Override
    public void setWorkerOptions(WorkerOptions workerOptions) {
        setBroker(workerOptions.getBrokerURL());
        setDuration(workerOptions.getDuration());
    }

    public void start() {
        logger.info("Starting the test");

        try {
            JMSReceiverClient client = new JMSReceiverClient();

            client.setUrl(url);

            running = true;
            client.start();

            Instant startTime = Instant.now();

            long count = 0;

            WorkerSnapshot snapshot = new WorkerSnapshot();

            snapshot.setStartTime(startTime);


            while (duration.canContinue(snapshot) && isRunning()) {
                snapshot.setCount(count);

                Instant now = Instant.now();
                snapshot.setNow(now);

                count++;
                MessageInfo info = client.receiveMessages();
                logger.trace("Received: {}", info);
            }
        } catch (Exception e) {
            logger.error("Unable to start the worker: {}", e.getMessage(), e);
        }
        finally {
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
    public WorkerSnapshot stats() {
        return null;
    }

    @Override
    public void setQueue(BlockingQueue<WorkerSnapshot> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        start();
    }
}
