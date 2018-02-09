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

import org.maestro.common.URLQuery;
import org.maestro.common.content.ContentStrategy;
import org.maestro.common.jms.SenderClient;

import javax.jms.*;
import java.net.URI;
import java.nio.ByteBuffer;

final class JMSSenderClient extends JMSClient implements SenderClient {

    private ContentStrategy contentStrategy;
    private Session session;
    private MessageProducer producer;

    @Override
    public void start() throws Exception {
        super.start();
        try {
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.producer = session.createProducer(destination);
            final URLQuery urlQuery = new URLQuery(new URI(url));
            final boolean durable = urlQuery.getBoolean("durable", false);
            if (durable) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            final Integer priority = urlQuery.getInteger("priority", null);
            if (priority != null) {
                producer.setPriority(priority);
            }
            final Long ttl = urlQuery.getLong("ttl", null);
            if (ttl != null) {
                producer.setTimeToLive(ttl);
            }
            producer.setDisableMessageTimestamp(true);
        } catch (Throwable t) {
            JMSResourceUtil.capturingClose(this.producer);
            this.producer = null;
            JMSResourceUtil.capturingClose(this.session);
            this.session = null;
            JMSResourceUtil.capturingClose(this.connection);
            this.connection = null;
            throw t;
        }
    }

    @Override
    public void sendMessages(long sendTimeEpochInMicros) throws JMSException {
        //prepare the message content
        final ByteBuffer content = contentStrategy.prepareContent();
        final byte[] bytes = content.array();
        final int position = content.position();
        final int offset = content.arrayOffset() + position;
        final int length = content.remaining();
        //the timestamp is part of the message content
        content.putLong(position, sendTimeEpochInMicros);
        final BytesMessage message = session.createBytesMessage();
        //copy the whole message content (including the benchmark payload ie timestamp)
        message.writeBytes(bytes, offset, length);
        producer.send(message);
    }

    @Override
    public void setContentStrategy(ContentStrategy contentStrategy) {
        this.contentStrategy = contentStrategy;
    }

    @Override
    public void stop() {
        JMSResourceUtil.capturingClose(producer);
        this.producer = null;
        JMSResourceUtil.capturingClose(session);
        this.session = null;
        super.stop();
    }
}
