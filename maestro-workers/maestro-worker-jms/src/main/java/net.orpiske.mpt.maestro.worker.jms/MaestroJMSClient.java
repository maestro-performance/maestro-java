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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

public class MaestroJMSClient extends Client {
    public ConnectionFactory getFactory() {
        return factory;
    }

    public Destination getQueue() {
        return queue;
    }

    public String getOperation() {
        return operation;
    }

    public int getMessages() {
        return messages;
    }

    public int getBodySize() {
        return bodySize;
    }

    public int getTransactionSize() {
        return transactionSize;
    }

    public boolean isDurable() {
        return durable;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public int getReceived() {
        return received;
    }

    public void setReceived(int received) {
        this.received = received;
    }
}
