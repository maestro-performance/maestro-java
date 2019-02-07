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
package org.maestro.worker.tests.support.runner;

import org.apache.activemq.broker.BrokerService;

/**
 * This class configures the provider once it has been initialized
 */
public class AMQPBrokerConfiguration extends AbstractBrokerConfiguration {
    protected static final String CONNECTOR = "tcp://localhost:61616";

    @Override
    protected String getConnector() {
        return CONNECTOR;
    }

    @Override
    protected void configureConnectors(BrokerService brokerService) throws Exception {
        addConnector(brokerService, "mqtt://localhost:1883", "MQTT");
        addConnector(brokerService, "amqp://localhost:5672", "AMQP");
    }
}
