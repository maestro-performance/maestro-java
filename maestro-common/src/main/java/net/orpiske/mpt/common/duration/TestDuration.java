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

import net.orpiske.mpt.common.worker.WorkerSnapshot;


/**
 * Test duration abstracts the test duration. The test duration can be either
 * represented by a time or the number of messages
 */
public interface TestDuration {


    /**
     * Get the numeric time duration
     * @return the number of messages or the number of seconds for the test
     */
    long getNumericDuration();


    /**
     * Whether the test can continue based on the current snapshot
     * @param snapshot current snapshot
     * @return true if the test can continue or false otherwise
     */
    boolean canContinue(WorkerSnapshot snapshot);
}
