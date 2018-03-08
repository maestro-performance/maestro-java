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

package org.maestro.common.client.notes;

public enum MaestroOpt {
    /** Broker address */
    MAESTRO_NOTE_OPT_SET_BROKER,
    /** Duration type (count or duration).
     * Values are defined as parameters to the message
     */
    MAESTRO_NOTE_OPT_SET_DURATION_TYPE,
    /** Set the log level */
    MAESTRO_NOTE_OPT_SET_LOG_LEVEL,
    /** Set the parallel count */
    MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT,
    /** Set message size */
    MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE,
    /** Set throttle */
    MAESTRO_NOTE_OPT_SET_THROTTLE,
    /** Set rate */
    MAESTRO_NOTE_OPT_SET_RATE,
    /** Set fail condition  */
    MAESTRO_NOTE_OPT_FCL
}
