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

import net.orpiske.mpt.common.worker.MessageInfo;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.time.Instant;

public class JMSReceiverClient extends Client {
    private Session session;
    private MessageConsumer consumer;

    @Override
    void start() throws Exception {
        super.start();

        session = super.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(queue);
    }

    MessageInfo receiveMessages() throws JMSException {
        BytesMessage message = (BytesMessage) consumer.receive();

        if (message == null) {
            throw new RuntimeException("Null receive");
        }

        MessageInfo ret = new MessageInfo();

        long ctime = message.getLongProperty("SendTime");
        ret.setCreationTime(Instant.ofEpochMilli(ctime));

        return ret;
    }
}
