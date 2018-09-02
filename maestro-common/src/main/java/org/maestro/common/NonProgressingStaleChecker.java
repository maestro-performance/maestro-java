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

package org.maestro.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stale checker that checks if a count is non-progressing (ie.: if after a given number of retries
 * the count is still the same). It assumes that the checked value is ever increasing (ie.: a sum of
 * all messages sent)
 */
public class NonProgressingStaleChecker implements StaleChecker {
    private static final Logger logger = LoggerFactory.getLogger(NonProgressingStaleChecker.class);

    private final long retries;
    private long lastCount = 0;
    private long repeat = 0;

    /**
     * Constructor
     * @param retries the number of retries to do before considering the count as stale
     */
    public NonProgressingStaleChecker(long retries) {
        this.retries = retries;
    }

    public boolean isStale(long count) {
        if (count > lastCount) {
            lastCount = count;
        }
        else {
            if (count == lastCount) {
                repeat++;
                logger.trace("Current count is the same as last count. Checking if stale");

                if (repeat >= retries) {
                    logger.trace("Count is stale");

                    return true;
                }
            }
        }

        return false;
    }
}
