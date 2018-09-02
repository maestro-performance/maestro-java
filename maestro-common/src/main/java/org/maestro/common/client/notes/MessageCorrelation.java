/*
 * Copyright 2018 Otavio Rodolfo Piske
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
