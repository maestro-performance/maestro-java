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
import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.Stats;
import net.orpiske.mpt.common.writers.RateWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JMSSenderWorker implements MaestroSenderWorker {
    private static final Logger logger = LoggerFactory.getLogger(JMSSenderWorker.class);

    private ContentStrategy contentStrategy;
    private RateWriter rateWriter;

    private String url;
    private int messageSize;

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

    }

    public void start() {
        try {
            JMSSenderClient client;

            client = new JMSSenderClient();

            client.setUrl(url);
            client.setContentStrategy(contentStrategy);

            client.start();

            while (true) {
                client.sendMessages();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }

    public void halt() {

    }

    public Stats stats() {
        return null;
    }
}
