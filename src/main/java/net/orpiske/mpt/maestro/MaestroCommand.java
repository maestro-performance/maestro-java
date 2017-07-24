/*
 *  Copyright ${YEAR} ${USER}
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

package net.orpiske.mpt.maestro;

public enum MaestroCommand {
    /** Receiver execution **/
    MAESTRO_NOTE_START_RECEIVER(0),
    MAESTRO_NOTE_STOP_RECEIVER(1),
    /** Sender execution */
    MAESTRO_NOTE_START_SENDER(2),
    MAESTRO_NOTE_STOP_SENDER(3),
    /** Inspector execution **/
    MAESTRO_NOTE_START_INSPECTOR(4),
    MAESTRO_NOTE_STOP_INSPECTOR(5),
    MAESTRO_NOTE_FLUSH(6),
    MAESTRO_NOTE_SET(7),
    MAESTRO_NOTE_STATS(8),
    MAESTRO_NOTE_HALT(9),
    MAESTRO_NOTE_PING(10),
    MAESTRO_NOTE_OK(11),
    MAESTRO_NOTE_PROTOCOL_ERROR(12),
    MAESTRO_NOTE_INTERNAL_ERROR(13),
    MAESTRO_NOTE_ABNORMAL_DISCONNECT(14),

    /** Notifications */
    MAESTRO_NOTE_NOTIFY_FAIL(15),
    MAESTRO_NOTE_NOTIFY_SUCCESS(16);

    private long value;

    MaestroCommand(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
