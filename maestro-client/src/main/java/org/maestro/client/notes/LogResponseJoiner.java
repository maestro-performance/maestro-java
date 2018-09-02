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

package org.maestro.client.notes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LogResponseJoiner {
    private static final Logger logger = LoggerFactory.getLogger(LogResponseJoiner.class);
    private static final LogResponseJoiner instance = new LogResponseJoiner();
    private final Map<String, LogResponse> cache = new HashMap<>();

    public static LogResponseJoiner getInstance() {
        return instance;
    }

    public LogResponse join(final LogResponse logResponse) {
        String key = logResponse.getId() + "-" + logResponse.getLocationType() + "-" + logResponse.getFileName();
        logger.debug("Log response key: {}", key);

        final LogResponse prev = cache.get(key);
        if (prev == null) {
            logger.debug("A previous entry does not exist for file {}. Checking if is has additional chunks ...",
                    logResponse.getFileName());

            if (logResponse.hasNext()) {
                logger.debug("File {} has additional chunks. Caching and waiting", logResponse.getFileName());
                cache.put(key, logResponse);
            }
            else {
                logger.debug("No additional chunks are reported. The log response is complete for file {}",
                        logResponse.getFileName());
            }

            return logResponse;
        }
        else {
            logger.debug("A previous entry for file {} exists", logResponse.getFileName());
            prev.join(logResponse);
            prev.next();

            if (prev.isLast()) {
                logger.debug("The last chunk for file {} was successfully joined. Removing from cache and returning",
                        prev.getFileName());
                cache.remove(prev);
                prev.next();
            }

            return prev;
        }
    }

}
