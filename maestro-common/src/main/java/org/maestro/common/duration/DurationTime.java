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

import org.maestro.common.exceptions.DurationParseException;

import java.util.concurrent.TimeUnit;

/**
 * Time-based test duration object
 */
public class DurationTime implements TestDuration {
    private static final String DURATION_TYPE_NAME = "time";

    private final long expectedDuration;
    private final TimeUnit outputTimeUnit;
    private final String timeSpec;

    public DurationTime(final String timeSpec) throws DurationParseException {
        this.expectedDuration = DurationUtils.parse(timeSpec);
        this.timeSpec = timeSpec;
        this.outputTimeUnit = TimeUnit.SECONDS;
    }

    private DurationTime(final long seconds) {
        this.expectedDuration = seconds;
        this.timeSpec = seconds + "s";
        this.outputTimeUnit = TimeUnit.SECONDS;
    }

    @Override
    public boolean canContinue(final TestProgress snapshot) {
        final long currentDuration = snapshot.elapsedTime(outputTimeUnit);
        return currentDuration < expectedDuration;
    }

    @Override
    public long getNumericDuration() {
        return expectedDuration;
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
        return timeSpec;
    }

    @Override
    public String durationTypeName() {
        return DURATION_TYPE_NAME;
    }
}
