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

import net.orpiske.mpt.common.worker.MaestroReceiverWorker;
import net.orpiske.mpt.common.worker.WorkerSnapshot;
import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;

public class JMSReceiverWorker implements MaestroReceiverWorker {
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

    public WorkerSnapshot stats() {
        return null;
    }
}
