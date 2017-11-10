package net.orpiske.mpt.maestro.worker.quiver;

import java.net.URI;
import java.net.URISyntaxException;

public class QuiverArgumentBuilder {

    private QuiverArgumentBuilder() {}


    public static String[] buildArguments(final String role, final String brokerUrl, final String duration, final String messageSize) throws URISyntaxException {
        URI uri = null;

        uri = new URI(brokerUrl);

        System.setProperty("arrow.jms.url", brokerUrl);
        System.setProperty("java.naming.factory.initial", "org.apache.qpid.jms.jndi.JmsInitialContextFactory");

        String port = (uri.getPort() > 0 ? Integer.toString(uri.getPort()) : "-");

        String[] args = {
                "client",
                "active",
                role,
                "maestro",
                uri.getHost(),
                port,
                uri.getPath().substring(1),
                duration,
                messageSize,
                "1024",
                "0",
                ""};
        return args;
    }
}
