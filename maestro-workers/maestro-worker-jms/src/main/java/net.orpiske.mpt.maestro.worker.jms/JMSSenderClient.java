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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

final class JMSSenderClient extends Client {
    private static final Logger logger = LoggerFactory.getLogger(JMSSenderClient.class);

    private ContentStrategy contentStrategy;

    private Session session;
    private MessageProducer producer;

    @Override
    void start() throws Exception {
        super.start();

        session = super.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

        producer = session.createProducer(queue);

        if (durable) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } else {
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }

        producer.setDisableMessageTimestamp(true);
    }


    public void sendMessages() throws JMSException {
        TextMessage message = session.createTextMessage();

        final String content = contentStrategy.getContent();
        final long now = System.currentTimeMillis();

        message.setText(content);
        message.setLongProperty("SendTime", now);

        producer.send(message);
    }

    public ContentStrategy getContentStrategy() {
        return contentStrategy;
    }

    public void setContentStrategy(ContentStrategy contentStrategy) {
        this.contentStrategy = contentStrategy;
    }
}
