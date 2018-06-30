package org.maestro.client.notes;

import java.util.HashMap;
import java.util.Map;

public class LogResponseJoiner {
    private static final LogResponseJoiner instance = new LogResponseJoiner();
    private Map<String, LogResponse> cache = new HashMap<>();

    public static final LogResponseJoiner getInstance() {
        return instance;
    }

    public LogResponse join(final LogResponse logResponse) {
        String key = logResponse.getId() + "-" + logResponse.getLocationType() + "-" + logResponse.getFileName();

        final LogResponse prev = cache.get(key);
        if (prev == null) {
            if (logResponse.hasNext()) {
                cache.put(key, logResponse);
            }

            return logResponse;
        }
        else {
            prev.join(logResponse);

            if (!prev.hasNext()) {
                cache.remove(prev);

            }

            return prev;
        }
    }

}
