package org.maestro.common.client.notes;

import java.util.Objects;
import java.util.UUID;

public class MessageCorrelation {
    private final String correlationId;
    // Reserved for future use.
    private final String messageId;

    public MessageCorrelation(String correlationId, String messageId) {
        this.correlationId = correlationId;
        this.messageId = messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageCorrelation that = (MessageCorrelation) o;
        return Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, messageId);
    }

    public static MessageCorrelation newRandomCorrelation() {
        final String correlationId = UUID.randomUUID().toString();
        final String messageId = UUID.randomUUID().toString();

        return new MessageCorrelation(correlationId, messageId);
    }
}
