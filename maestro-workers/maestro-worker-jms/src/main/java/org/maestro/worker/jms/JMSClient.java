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

/*
  NOTE: this a fork of Justin Ross' Quiver at:
  https://raw.githubusercontent.com/ssorj/quiver/master/java/quiver-jms-driver/src/main/java/net/ssorj/quiver/QuiverArrowJms.java
  <p>
  The code was modified to integrate more tightly with maestro.
 */

package org.maestro.worker.jms;

import org.maestro.common.jms.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * A basic JMS client
 */
class JMSClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(JMSClient.class);

    private String url = null;
    private Destination destination = null;
    protected Connection connection = null;
    private JmsOptions opts;

    private int number = -1;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public JmsOptions getOpts() {
        return opts;
    }

    @Override
    public void start() throws Exception {
        logger.debug("Starting the JMS client");

        Connection connection = null;
        try {
            opts = new JmsOptions(url);
            final JMSProtocol protocol = opts.getProtocol();
            logger.debug("JMS client is running test with the protocol {}", protocol.name());

            logger.debug("JMS client is running with connection url {}", opts.getConnectionUrl());
            final ConnectionFactory factory = protocol.createConnectionFactory(opts.getConnectionUrl());
            logger.trace("Connection factory created");

            String destinationName = opts.getPath().substring(1);
            logger.debug("Requested destination name: {}", destinationName);

            destinationName = setupLimitDestinations(destinationName, opts.getConfiguredLimitDestinations(), getNumber());
            this.destination = createDestination(protocol, destinationName);

            logger.debug("Creating the connection");
            connection = factory.createConnection();
            logger.debug("Connection created successfully");
        } catch (Throwable t) {
            logger.warn("Something wrong happened while initializing the JMS client: {}", t.getMessage(), t);

            JMSResourceUtil.capturingClose(connection);
            throw t;
        }

        this.connection = connection;
        this.connection.start();
    }

    private Destination createDestination(final JMSProtocol protocol, final String destinationName) {
        Destination destination;
        final String type = opts.getType();

        switch (type) {
            case "queue":
                logger.debug("Creating a queue-based destination");
                destination = protocol.createQueue(destinationName);
                break;
            case "topic":
                logger.debug("Creating a topic-based destination");
                destination = protocol.createTopic(destinationName);
                break;
            default:
                throw new UnsupportedOperationException("not supported destination type: " + type);
        }

        return destination;
    }

    public static String setupLimitDestinations(final String destinationName, final int limitDestinations,
                                                 final int clientNumber) {
        String ret = destinationName;

        if (limitDestinations >= 1) {
            logger.debug("Client requested a client-specific limit to the number of destinations: {}",
                    limitDestinations);

            final int destinationId = clientNumber % limitDestinations;
            ret = destinationName + '.' + destinationId;
            logger.info("Requested destination name after using client-specific limit to the number of destinations: {}",
                    ret);

            return ret;
        } else {
            if (limitDestinations < 0) {
                throw new IllegalArgumentException("Negative number of limit destinations is invalid");
            }

            //original behaviour maintained for backward compatibility
            logger.info("Requested destination name: {}", destinationName);
        }

        return ret;
    }

    @Override
    public void stop() {
        logger.debug("Stopping the JMS client");
        final Throwable t = JMSResourceUtil.capturingClose(connection);
        this.connection = null;
        if (t != null) {
            logger.warn("Error closing the connection: {}", t.getMessage(), t);
        }
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    protected String getUrl() {
        return url;
    }

    protected Destination getDestination() {
        return destination;
    }

    protected Connection getConnection() {
        return connection;
    }
}
