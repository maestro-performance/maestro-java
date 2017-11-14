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

import net.orpiske.mpt.common.worker.MaestroSenderWorker;
import net.orpiske.mpt.common.worker.Stats;
import net.orpiske.mpt.common.writers.RateWriter;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

public class JMSSenderWorker implements MaestroSenderWorker {
    private MaestroJMSClient client;

    public JMSSenderWorker() {

//        String url = System.getProperty("arrow.jms.url");
//        assert url != null;
//
//        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
//        env.put("connectionFactory.ConnectionFactory", url);
//        env.put("queue.queueLookup", path);
//
//        Context context = new InitialContext(env);;
//        ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
//        Destination queue = (Destination) context.lookup("queueLookup");
    }

    public RateWriter getRateWriter() {
        return null;
    }

    public void setRateWriter(RateWriter rateWriter) {

    }

    public void setBroker(String url) {

    }

    public void setDuration(String duration) {

    }

    public void setLogLevel(String logLevel) {

    }

    public void setParallelCount(String parallelCount) {

    }

    public void setMessageSize(String messageSize) {

    }

    public void setThrottle(String value) {

    }

    public void setRate(String rate) {

    }

    public void start() {

    }

    public void stop() {

    }

    public void halt() {

    }

    public Stats stats() {
        return null;
    }
}
