/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.worker.jms.rabbitmq;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

/**
 * RabbitMQ connection factory uses a different constructor signature.
 * This class abstracts it to make it compatible
 */
@SuppressWarnings("serial")
public class MaestroRabbitMQConnectionFactory extends RMQConnectionFactory {

    @SuppressWarnings("unused")
    public MaestroRabbitMQConnectionFactory(final String url) {
        super();
    }
}
