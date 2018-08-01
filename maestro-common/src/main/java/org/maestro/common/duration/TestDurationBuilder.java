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


/**
 * Test duration builder
 */
public class TestDurationBuilder {

    /**
     * Builds the appropriate test duration based on a duration specifier
     * @param durationSpec a duration specifier (either time based such as "1h2m" or
     *                     a numeric string representing the number of messages to send
     * @return The appropriate TestDuration object according to the duration specifier
     * @throws DurationParseException if the duration specification is invalid
     */
    public static TestDuration build(final String durationSpec) throws DurationParseException {
        if (durationSpec == null) {
            throw new DurationParseException("Invalid duration: null");
        }

        if (durationSpec.matches(".*[a-zA-Z].*")) {
            return new DurationTime(durationSpec);
        }
        else {
            if (durationSpec.equals(DurationDrain.DURATION_DRAIN_FORMAT)) {
                return new DurationDrain();
            }

            return new DurationCount(durationSpec);
        }
    }
}
