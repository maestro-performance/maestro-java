/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.agent.base;


/**
 * Agent constants.
 */
@SuppressWarnings("unused")
class AgentConstants {
    /**
     * Agent basic constants.
     */
    static final String EXTENSION_POINT = "ext";

    /**
     * Basic commands constants.
     */
    static final String ABNORMAL_DISCONNECT = "abnormaldisconnect";

    static final String FLUSH = "flush";

    static final String GET = "get";

    static final String HALT = "halt";

    static final String NOTIFY_FAIL = "notifyfail";

    static final String NOTIFY_SUCCESS = "notifysuccess";

    static final String PING = "ping";

    static final String SET = "set";

    static final String START_INSPECTOR = "startinspector";

    static final String START_WORKER = "startworker";

    @Deprecated
    static final String START_SENDER = "startsender";

    static final String STATS = "stats";

    static final String STOP_INSPECTOR = "stopinspector";

    static final String STOP_WORKER = "stopworker";

    @Deprecated
    static final String STOP_SENDER = "stopsender";

    static final String START_AGENT = "startagent";

    static final String START_STOP = "stopagent";

    static final String USER_COMMAND_1 = "usercommand1";

    static final String OK = "ok";

    static final String PROTOCOL_ERROR = "protocolerror";

    static final String INTERNAL_ERROR = "internalerror";

    static final String DRAIN = "drain";
}
