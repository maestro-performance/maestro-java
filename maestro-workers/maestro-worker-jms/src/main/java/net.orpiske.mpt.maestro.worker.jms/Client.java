/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * NOTE: this a fork of Justin Ross' Quiver at:
 * https://raw.githubusercontent.com/ssorj/quiver/master/java/quiver-jms-driver/src/main/java/net/ssorj/quiver/QuiverArrowJms.java
 *
 * The code was modified to integrate more tightly with maestro.
 */

package net.orpiske.mpt.maestro.worker.jms;

import javax.jms.*;
import java.io.PrintWriter;
import java.util.Arrays;

class Client {
    protected ConnectionFactory factory;
    protected Destination queue;
    protected String operation;
    protected int messages;
    protected int bodySize;
    protected int transactionSize;

    protected boolean durable;

    protected int sent;
    protected int received;

    Client() {}

    Client(ConnectionFactory factory, Destination queue, String operation,
           int messages, int bodySize, int transactionSize, String[] flags) {
        this.factory = factory;
        this.queue = queue;
        this.operation = operation;
        this.messages = messages;
        this.bodySize = bodySize;
        this.transactionSize = transactionSize;

        this.durable = Arrays.asList(flags).contains("durable");

        this.sent = 0;
        this.received = 0;
    }

    void run() {
        try {
            Connection conn = factory.createConnection();
            conn.start();

            final Session session;

            if (transactionSize > 0) {
                session = conn.createSession(true, Session.SESSION_TRANSACTED);
            } else {
                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }

            if (operation.equals("send")) {
                sendMessages(session);
            } else if (operation.equals("receive")) {
                receiveMessages(session);
            } else {
                throw new java.lang.IllegalStateException();
            }

            if (transactionSize > 0) {
                session.commit();
            }

            conn.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private static PrintWriter getOutputWriter() {
        return new PrintWriter(System.out);
    }

    void sendMessages(Session session) throws JMSException {
        PrintWriter out = getOutputWriter();
        MessageProducer producer = session.createProducer(queue);

        if (durable) {
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        } else {
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }

        producer.setDisableMessageTimestamp(true);

        byte[] body = new byte[bodySize];
        Arrays.fill(body, (byte) 120);

        while (sent < messages) {
            BytesMessage message = session.createBytesMessage();
            long stime = System.currentTimeMillis();

            message.writeBytes(body);
            message.setLongProperty("SendTime", stime);

            producer.send(message);

            out.printf("%s,%d\n", message.getJMSMessageID(), stime);

            sent += 1;

            if (transactionSize > 0 && (sent % transactionSize) == 0) {
                session.commit();
            }
        }

        out.flush();
    }

    void receiveMessages(Session session) throws JMSException {
        PrintWriter out = getOutputWriter();
        MessageConsumer consumer = session.createConsumer(queue);

        while (received < messages) {
            BytesMessage message = (BytesMessage) consumer.receive();

            if (message == null) {
                throw new RuntimeException("Null receive");
            }

            String id = message.getJMSMessageID();
            long stime = message.getLongProperty("SendTime");
            long rtime = System.currentTimeMillis();

            out.printf("%s,%d,%d\n", id, stime, rtime);

            received += 1;

            if (transactionSize > 0 && (received % transactionSize) == 0) {
                session.commit();
            }
        }

        out.flush();
    }
}
