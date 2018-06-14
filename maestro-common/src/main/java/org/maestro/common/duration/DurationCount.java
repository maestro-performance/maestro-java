/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.common.duration;

import org.apache.commons.configuration.AbstractConfiguration;
import org.maestro.common.ConfigurationWrapper;

/**
 * Count-based test duration object
 */
public class DurationCount implements TestDuration {
    private static final AbstractConfiguration config = ConfigurationWrapper.getConfig();
    public static final long WARM_UP_COUNT;
    private static final String DURATION_TYPE_NAME = "count";

    static {
        WARM_UP_COUNT = config.getLong("warm-up.message.count", 1000000);
    }

    private final long count;

    public DurationCount(final String durationSpec) {
        this.count = Long.parseLong(durationSpec);
    }

    DurationCount(long count) {
        this.count = count;
    }

    @Override
    public boolean canContinue(final TestProgress progress) {
        return progress.messageCount() < count;

    }

    @Override
    public long getNumericDuration() {
        return count;
    }

    @Override
    public TestDuration getWarmUpDuration() {
        return DurationUtils.DEFAULT_WARM_UP_DURATION;
    }

    @Override
    public TestDuration getCoolDownDuration() {
        return getWarmUpDuration();
    }

    public String toString() {
        return Long.toString(count);
    }

    @Override
    public String durationTypeName() {
        return DURATION_TYPE_NAME;
    }
}
