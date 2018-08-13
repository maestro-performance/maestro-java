/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.maestro.worker.jms;

import org.maestro.worker.jms.rabbitmq.MaestroRabbitMQConnectionFactory;
import org.maestro.worker.jms.rabbitmq.MaestroRabbitMQQueueFactory;
import org.maestro.worker.jms.rabbitmq.MaestroRabbitMQTopicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.function.Function;

/**
 * JMS Protocol
 */
@SuppressWarnings("unused")
public enum JMSProtocol {
    ARTEMIS(
            org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory::new,
            org.apache.activemq.artemis.jms.client.ActiveMQQueue::new,
            org.apache.activemq.artemis.jms.client.ActiveMQTopic::new),

    AMQP(
            org.apache.qpid.jms.JmsConnectionFactory::new,
            org.apache.qpid.jms.JmsQueue::new,
            org.apache.qpid.jms.JmsTopic::new),
    OPENWIRE(
            org.apache.activemq.ActiveMQConnectionFactory::new,
            org.apache.activemq.command.ActiveMQQueue::new,
            org.apache.activemq.command.ActiveMQTopic::new),
    RABBITAMQP(
            MaestroRabbitMQConnectionFactory::new,
            MaestroRabbitMQQueueFactory::new,
            MaestroRabbitMQTopicFactory::new);

    private static final Logger logger = LoggerFactory.getLogger(JMSProtocol.class);

    private final Function<String, ? extends ConnectionFactory> factory;
    private final Function<String, ? extends Queue> queueFactory;
    private final Function<String, ? extends Topic> topicFactory;

    JMSProtocol(Function<String, ? extends ConnectionFactory> factory,
                Function<String, ? extends Queue> queueFactory,
                Function<String, ? extends Topic> topicFactory) {
        this.factory = factory;
        this.queueFactory = queueFactory;
        this.topicFactory = topicFactory;
    }

    ConnectionFactory createConnectionFactory(String uri) {
        logger.debug("Creating an {} connection to {}", this.name(), uri);

        return factory.apply(uri);
    }

    Queue createQueue(String name) {
        return this.queueFactory.apply(name);
    }

    Topic createTopic(String name) {
        return this.topicFactory.apply(name);
    }
}