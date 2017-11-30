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
import net.orpiske.mpt.common.jms.ReceiverClient;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.nio.ByteBuffer;

final class JMSReceiverClient extends JMSClient implements ReceiverClient {
    private static final long RECEIVE_TIMEOUT_MILLIS = 1000L;
    private static final int PAYLOAD_SIZE = Long.BYTES;
    private Session session;
    private MessageConsumer consumer;
    private ByteBuffer payloadBytes;

    @Override
    public void start() throws Exception {
        super.start();

        session = super.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(queue);
        payloadBytes = ByteBuffer.allocate(PAYLOAD_SIZE).order(ContentStrategy.CONTENT_ENDIANNESS);
    }


    @Override
    public long receiveMessages() throws Exception {
        final Message message = consumer.receive(RECEIVE_TIMEOUT_MILLIS);

        if (message == null) {
            return ReceiverClient.noMessagePayload();
        }
        final BytesMessage bytesMessage = (BytesMessage) message;
        //just read the benchmark minimum payload
        final int readBytes = bytesMessage.readBytes(payloadBytes.array(), PAYLOAD_SIZE);
        if (readBytes == PAYLOAD_SIZE || readBytes == -1) {
            //can read the timestamp using the default endianness of the content strategy
            final long sendTime = payloadBytes.getLong(0);
            return sendTime;
        }
        throw new IllegalStateException("the received message hasn't any benchmark payload");
    }
}
