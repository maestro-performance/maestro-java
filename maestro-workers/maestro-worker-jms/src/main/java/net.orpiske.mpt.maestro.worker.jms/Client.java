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
import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.URI;
import java.util.Hashtable;

class Client {
    private Hashtable<Object, Object> env = new Hashtable<Object, Object>();

    private String url;
    protected Destination queue;
    protected int bodySize;

    protected boolean durable = false;

    protected Connection conn;

    Client() {}

    void start() throws Exception {
        env.put("connectionFactory.ConnectionFactory", url);

        URI uri = new URI(url);
        String path = uri.getPath().substring(1);

        env.put("queue.queueLookup", path);

        Context context = new InitialContext(env);

        ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");

        Destination queue = (Destination) context.lookup("queueLookup");
        setQueue(queue);

        conn = factory.createConnection();
        conn.start();
    }

    void stop() {
        try {
            conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public Destination getQueue() {
        return queue;
    }

    public void setQueue(Destination queue) {
        this.queue = queue;
    }


    public int getBodySize() {
        return bodySize;
    }

    public boolean isDurable() {
        return durable;
    }

    protected Connection getConnection() {
        return conn;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
