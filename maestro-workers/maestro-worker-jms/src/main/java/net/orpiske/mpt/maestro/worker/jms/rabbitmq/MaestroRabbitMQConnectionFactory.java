package net.orpiske.mpt.maestro.worker.jms.rabbitmq;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

/**
 * RabbitMQ connection factory uses a different constructor signature.
 * This class abstracts it to make it compatible
 */
public class MaestroRabbitMQConnectionFactory extends RMQConnectionFactory {
    public MaestroRabbitMQConnectionFactory(final String url) {
        super();
    }
}
