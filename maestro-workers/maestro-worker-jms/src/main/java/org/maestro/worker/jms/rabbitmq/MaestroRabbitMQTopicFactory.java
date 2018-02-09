package org.maestro.worker.jms.rabbitmq;

import com.rabbitmq.jms.admin.RMQDestination;


/**
 * RabbitMQ topic factory uses a different constructor signature. This
 * class abstracts it to make it compatible
 */
public class MaestroRabbitMQTopicFactory extends RMQDestination {
    public MaestroRabbitMQTopicFactory(final String queue) {
        super(queue, false, false);
    }


}
