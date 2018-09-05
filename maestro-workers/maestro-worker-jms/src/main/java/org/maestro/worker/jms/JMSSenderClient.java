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

import org.maestro.common.content.ContentStrategy;
import org.maestro.common.jms.SenderClient;

import javax.jms.*;
import java.nio.ByteBuffer;

final class JMSSenderClient extends JMSClient implements SenderClient {

    private ContentStrategy contentStrategy;
    private Session session;
    private MessageProducer producer;

    @Override
    public void start() throws Exception {
        super.start();
        try {
            this.session = connection.createSession(getOpts().getSessionMode() == Session.SESSION_TRANSACTED, getOpts().getSessionMode());
            this.producer = session.createProducer(getDestination());

            setupMessageDurability();
            setupPriority();
            setupTTL();
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

    private void setupMessageDurability() throws JMSException {
        final boolean durable = getOpts().isDurable();

        if (durable) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } else {
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }
    }

    private void setupTTL() throws JMSException {
        // Ref: https://docs.oracle.com/cd/E17802_01/products/products/jms/javadoc-102a/javax/jms/MessageProducer.html#setTimeToLive(long)
        final int defaultTTL = 0;

        final long ttl = getOpts().getTtl();
        if (ttl > defaultTTL) {
            producer.setTimeToLive(ttl);
        }
        else {
            if (ttl < defaultTTL) {
                throw new IllegalArgumentException("Invalid TTL value: " + ttl);
            }
        }
    }

    private void setupPriority() throws JMSException {
        // Ref: https://docs.oracle.com/cd/E17802_01/products/products/jms/javadoc-102a/javax/jms/MessageProducer.html#setPriority(int)
        final int minPriority = 0;
        final int maxPriority = 9;

        final int priority = getOpts().getPriority();
        if (priority > minPriority) {
            if (priority < maxPriority) {
                producer.setPriority(priority);
            }
            else {
                throw new IllegalArgumentException("Invalid priority value: " + priority);
            }
        }
    }

    @Override
    public void sendMessages(long sendTimeEpochInMicros, boolean commitTransaction) throws JMSException {
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
        if (commitTransaction) {
            session.commit();
        }
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
