package org.maestro.worker.jms.rabbitmq;

import com.rabbitmq.jms.admin.RMQDestination;


/**
 * RabbitMQ queue factory uses a different constructor signature. This
 * class abstracts it to make it compatible
 */
public class MaestroRabbitMQQueueFactory extends RMQDestination {
    public MaestroRabbitMQQueueFactory(final String queue) {
        super(queue, true, false);
    }


}
