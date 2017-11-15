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

package net.orpiske.mpt.common.duration;

import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.worker.WorkerSnapshot;

import java.time.Instant;

/**
 * Time-based test duration object
 */
public class DurationTime implements TestDuration {
    private Instant end;
    private long numeric;
    private String timeSpec;

    public DurationTime(final String timeSpec) throws DurationParseException {
        this.numeric = DurationUtils.parse(timeSpec);
        this.timeSpec = timeSpec;
    }

    public boolean canContinue(WorkerSnapshot snapshot) {
        Instant now = snapshot.getNow();

        if (now.isAfter(end) || now.equals(end)) {
            return false;
        }

        return true;
    }

    public long getNumericDuration() {
        return numeric;
    }

    public String toString() {
        return timeSpec;
    }
}
