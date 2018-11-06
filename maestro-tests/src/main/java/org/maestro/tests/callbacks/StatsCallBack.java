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

package org.maestro.tests.callbacks;

import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.StatsResponse;
import org.maestro.common.Role;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.duration.DurationCount;
import org.maestro.tests.rate.FixedRateTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class StatsCallBack implements MaestroNoteCallback {
    private static final Logger logger = LoggerFactory.getLogger(StatsCallBack.class);

    private final FixedRateTestExecutor executor;
    private final Map<String, Long> counters = new HashMap<>();

    private final int targetRate;

    public StatsCallBack(FixedRateTestExecutor executor) {
        this.executor = executor;

        targetRate = executor.getTestProfile().getRate();
    }

    @Override
    public boolean call(final MaestroNote note) {
        if (!executor.isWarmUp() || !executor.isRunning()) {
            logger.trace("The stats callback should be not called outside warm-up phase");

            if (note instanceof StatsResponse) {
                return false;
            }

            return true;
        }

        if (note instanceof StatsResponse) {
            StatsResponse statsResponse = (StatsResponse) note;
            if (logger.isTraceEnabled()) {
                logger.trace("Received stats {}", statsResponse);
            }


            if (isSlow(statsResponse, targetRate)) {
                logger.debug("The warm-up duration might expire of time instead of count because the current " +
                                "rate {} is much lower than the target rate {}", statsResponse.getRate(),
                        targetRate);
            }

            updateCounters(statsResponse);

            completeCheck();

            return false;
        }

        return true;
    }

    private void completeCheck() {
        final long messageCount = counters.values().stream().mapToLong(Number::longValue).sum();
        logger.debug("Current message count: {}", messageCount);

        if (messageCount >= DurationCount.WARM_UP_COUNT) {
            logger.info("The warm-up count has been reached: {} of {}",
                    messageCount, DurationCount.WARM_UP_COUNT);
        }
        else {
            final int maxDuration = 3;
            Instant now = Instant.now();

            Duration elapsed = Duration.between(now, executor.getStartTime());
            if (elapsed.getSeconds() > (Duration.ofMinutes(maxDuration).getSeconds())) {
                logger.warn("Stopping the warm-up because the maximum duration was reached");
            }
        }
    }


    private boolean isSlow(StatsResponse statsResponse, int targetRate) {
        return (statsResponse.getRate() < ((double) targetRate / 2.0)) && statsResponse.getRate() > 0;
    }

    private void updateCounters(StatsResponse statsResponse) {
        final Role role = statsResponse.getPeerInfo().getRole();
        if (role == Role.INSPECTOR || role == Role.AGENT) {
            return;
        }

        final String key = statsResponse.getPeerInfo().prettyName();
        Long nodeCount = counters.get(key);
        if (nodeCount == null) {
            nodeCount = statsResponse.getCount();
        }
        else {
            nodeCount += statsResponse.getCount();
        }

        counters.put(key, nodeCount);
    }
}
