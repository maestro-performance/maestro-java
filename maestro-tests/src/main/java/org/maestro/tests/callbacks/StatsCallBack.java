package org.maestro.tests.callbacks;

import org.maestro.client.callback.MaestroNoteCallback;
import org.maestro.client.notes.StatsResponse;
import org.maestro.common.NodeUtils;
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

    public StatsCallBack(FixedRateTestExecutor executor) {
        this.executor = executor;
    }

    private void reset() {
        counters.clear();
    }

    @Override
    public boolean call(MaestroNote note) {
        if (!executor.isWarmUp() || !executor.isRunning()) {
            return true;
        }

        if (note instanceof StatsResponse) {
            StatsResponse statsResponse = (StatsResponse) note;
            logger.debug("Received stats {}", statsResponse);

            int targetRate = executor.getTestProfile().getRate();
            if (statsResponse.getRate() < (targetRate / 2) && statsResponse.getRate() > 0) {
                logger.warn("The warm-up duration might expire of time instead of count because the current " +
                                "rate {} is much lower than the target rate {}", statsResponse.getRate(),
                        executor.getTestProfile().getRate());
            }

            updateCounters(statsResponse);

            long messageCount = counters.values().stream().mapToLong(Number::longValue).sum();
            logger.debug("Current message count: {}", messageCount);
            if (messageCount >= DurationCount.WARM_UP_COUNT) {
                logger.info("The warm-up count has been reached: {} of {}",
                        messageCount, DurationCount.WARM_UP_COUNT);
                this.executor.stopServices();
                reset();
            }
            else {
                final int maxDuration = 3;
                Instant now = Instant.now();

                Duration elapsed = Duration.between(now, executor.getStartTime());
                if (elapsed.getSeconds() > (Duration.ofMinutes(maxDuration).getSeconds())) {
                    logger.warn("Stopping the warm-up because the maximum duration was reached");

                    this.executor.stopServices();
                    reset();
                }
            }

            return false;
        }

        return true;
    }

    private void updateCounters(StatsResponse statsResponse) {
        final String name = statsResponse.getName();
        String type = NodeUtils.getTypeFromName(name);
        if (type.equals("inspector") || type.equals("agent")) {
            return;
        }

        Long nodeCount = counters.get(name);
        if (nodeCount == null) {
            nodeCount = statsResponse.getCount();
        }
        else {
            nodeCount += statsResponse.getCount();
        }

        counters.put(name, nodeCount);
    }
}
