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

import java.util.concurrent.TimeUnit;


/**
 * Test duration abstracts the test duration. The test duration can be either
 * represented by a time or the number of messages
 */
public interface TestDuration {

    interface TestProgress {

        /**
         * It represent the start of the test in milliseconds using Unix Epoch in milliseconds.
         * <p>
         * If the test isn't started yet it is an arbitrary negative value.
         * @return the started time in milliseconds from epoch
         */
        long startedEpochMillis();

        /**
         * It represents the works done expressed in unit of time.
         * @param outputTimeUnit the output time unit (seconds, minutes, etc)
         * @return the elapsed time in the given time unit
         */
        default long elapsedTime(TimeUnit outputTimeUnit) {
            final long startedEpochMillis = startedEpochMillis();
            if (startedEpochMillis < 0) {
                return 0;
            } else {
                final long elapsedMillis = System.currentTimeMillis() - startedEpochMillis;
                return outputTimeUnit.convert(elapsedMillis, TimeUnit.MILLISECONDS);
            }
        }

        /**
         * It represents the works done expressed in messages processed.
         * @return the number of messages processed
         */
        long messageCount();

    }


    /**
     * Get the numeric time duration
     *
     * @return the number of messages or the number of seconds for the test
     */
    long getNumericDuration();

    /**
     * Get the warm up duration
     *
     * @return the warm up duration
     */
    TestDuration getWarmUpDuration();


    /**
     * Get the cool down duration
     *
     * @return the cool down duration
     */
    TestDuration getCoolDownDuration();


    /**
     * Whether the test can continue based on the current test's progress
     *
     * @param progress current progresses
     * @return true if the test can continue or false otherwise
     */
    boolean canContinue(TestProgress progress);

    /**
     * Gets the type name for the duration (ie.: "time", "count", etc). This
     * is a helper method for the front-ends parsing properties files and
     * having to identify the proper duration time specified
     *
     * @return The duration type name
     */
    String durationTypeName();
}
