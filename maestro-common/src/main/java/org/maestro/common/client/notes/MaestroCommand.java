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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MaestroCommand {
    /** Receiver execution **/
    MAESTRO_NOTE_START_WORKER(0),
    MAESTRO_NOTE_STOP_WORKER(1),
    /** Test execution */
    MAESTRO_NOTE_START_TEST(2),
    MAESTRO_NOTE_STOP_TEST(3),
    /** Inspector execution **/
    MAESTRO_NOTE_START_INSPECTOR(4),
    MAESTRO_NOTE_STOP_INSPECTOR(5),
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
    MAESTRO_NOTE_NOTIFY_SUCCESS(16),

    // New commands with v >= 1.3
    /* Get request */
    MAESTRO_NOTE_GET(17),

    /** Agent execution **/
    MAESTRO_NOTE_START_AGENT(18),
    MAESTRO_NOTE_STOP_AGENT(19),
    MAESTRO_NOTE_AGENT_SOURCE(21),
    MAESTRO_NOTE_USER_COMMAND_1(30),
    MAESTRO_NOTE_LOG(40),
    MAESTRO_NOTE_DRAIN(41),
    MAESTRO_NOTE_NOTIFY_DRAIN_COMPLETE(42),

    /** Group request */
    MAESTRO_NOTE_GROUP_JOIN(50),
    MAESTRO_NOTE_GROUP_LEAVE(51),

    MAESTRO_NOTE_ROLE_ASSIGN(60),
    MAESTRO_NOTE_ROLE_UNASSIGN(61);


    private final long value;

    MaestroCommand(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static MaestroCommand from(long value) {
        switch ((int) value) {
            case 0: return MAESTRO_NOTE_START_WORKER;
            case 1: return MAESTRO_NOTE_STOP_WORKER;
            case 2: return MAESTRO_NOTE_START_TEST;
            case 3: return MAESTRO_NOTE_STOP_TEST;
            case 4: return MAESTRO_NOTE_START_INSPECTOR;
            case 5: return MAESTRO_NOTE_STOP_INSPECTOR;
            case 7: return MAESTRO_NOTE_SET;
            case 8: return MAESTRO_NOTE_STATS;
            case 9: return MAESTRO_NOTE_HALT;
            case 10: return MAESTRO_NOTE_PING;
            case 11: return MAESTRO_NOTE_OK;
            case 12: return MAESTRO_NOTE_PROTOCOL_ERROR;
            case 13: return MAESTRO_NOTE_INTERNAL_ERROR;
            case 14: return MAESTRO_NOTE_ABNORMAL_DISCONNECT;
            case 15: return MAESTRO_NOTE_NOTIFY_FAIL;
            case 16: return MAESTRO_NOTE_NOTIFY_SUCCESS;
            case 17: return MAESTRO_NOTE_GET;
            case 18: return MAESTRO_NOTE_START_AGENT;
            case 19: return MAESTRO_NOTE_STOP_AGENT;
            case 21: return MAESTRO_NOTE_AGENT_SOURCE;
            case 30: return MAESTRO_NOTE_USER_COMMAND_1;
            case 40: return MAESTRO_NOTE_LOG;
            case 41: return MAESTRO_NOTE_DRAIN;
            case 42: return MAESTRO_NOTE_NOTIFY_DRAIN_COMPLETE;
            case 50: return MAESTRO_NOTE_GROUP_JOIN;
            case 60: return MAESTRO_NOTE_ROLE_ASSIGN;
            case 61: return MAESTRO_NOTE_ROLE_UNASSIGN;
            default: {
                Logger logger = LoggerFactory.getLogger(MaestroCommand.class);
                logger.error("The command {} is not implemented. This is a bug in Maestro", value);
            }

        }

        return null;
    }
}
