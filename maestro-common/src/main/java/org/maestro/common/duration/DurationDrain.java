/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.common.duration;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;
import org.maestro.common.NonProgressingStaleChecker;
import org.maestro.common.StaleChecker;

public class DurationDrain extends DurationCount {
    public static final String DURATION_DRAIN_FORMAT = "-1";

    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();

    private final StaleChecker staleChecker;

    public DurationDrain() {
        super(-1);

        int drainRetries = config.getInt("worker.auto.drain.retries", 10);
        staleChecker = new NonProgressingStaleChecker(drainRetries);
    }

    @Override
    public boolean canContinue(TestProgress progress) {
        long count = progress.messageCount();

        return !staleChecker.isStale(count);
    }
}
