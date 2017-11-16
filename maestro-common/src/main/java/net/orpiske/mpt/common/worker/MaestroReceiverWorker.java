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

package net.orpiske.mpt.common.worker;

import net.orpiske.mpt.common.writers.LatencyWriter;
import net.orpiske.mpt.common.writers.RateWriter;

/**
 * An interface for implementing maestro receivers
 */
public interface MaestroReceiverWorker extends MaestroWorker {


    /**
     * Gets the rate writer
     * @return
     */
    RateWriter getRateWriter();

    /**
     * Sets the rate writer
     * @param rateWriter
     */
    void setRateWriter(RateWriter rateWriter);

    /**
     * Sets the latency writer
     * @param latencyWriter
     */
    void setLatencyWriter(LatencyWriter latencyWriter);

    /**
     * Gets the latency writer
     * @return
     */
    LatencyWriter getLatencyWriter();

}
