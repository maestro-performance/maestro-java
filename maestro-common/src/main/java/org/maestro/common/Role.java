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
 *
 */

package org.maestro.common;

/**
 * A numeric representation of the role as used by the Maestro protocol and
 * Maestro data files
 */
public enum Role {
    /**
     * Other role types
     */
    OTHER(0),

    /**
     * Sender
     */
    SENDER(1),

    /**
     * Receiver
     */
    RECEIVER(2),

    /**
     * Inspector
     */
    INSPECTOR(3),

    /**
     * Agent
     */
    AGENT(4),

    /**
     * Exporter
     */
    EXPORTER(5),

    /**
     * Reports server
     */
    REPORTS_SERVER(6);


    private final int code;

    /**
     * Builds a new role type
     * @param code role code
     */
    Role(int code) {
        this.code = code;
    }


    /**
     * Gets the role numeric code
     * @return the role numeric code
     */
    public int getCode() {
        return code;
    }


    /**
     * Builds a new Role type from an integer
     * @param code the integer to build from
     * @return A new Role object
     */
    public static Role from(int code) {
        switch (code) {
            case 0: {
                return OTHER;
            }
            case 1: {
                return SENDER;
            }
            case 2: {
                return RECEIVER;
            }
            case 3: {
                return INSPECTOR;
            }
            case 4: {
                return AGENT;
            }
            case 5: {
                return EXPORTER;
            }
            case 6: {
                return REPORTS_SERVER;
            }
            default: {
                return OTHER;
            }
        }
    }

    public String toString() {
        switch (code) {
            case 0:
                return HostTypes.OTHER_HOST_TYPE;
            case 1:
                return HostTypes.SENDER_HOST_TYPE;
            case 2:
                return HostTypes.RECEIVER_HOST_TYPE;
            case 3:
                return HostTypes.INSPECTOR_HOST_TYPE;
            case 4:
                return HostTypes.AGENT_HOST_TYPE;
            case 5:
                return HostTypes.EXPORTER_HOST_TYPE;
            case 6:
                return HostTypes.REPORTS_SERVER_HOST_TYPE;

        }

        return HostTypes.OTHER_HOST_TYPE;
    }

    public boolean isWorker() {
        return this == SENDER || this == RECEIVER;
    }

    /**
     * Builds a new Role type from a String (check HostTypes class for details)
     * @param name the string to build from
     * @return A new Role object
     */
    public static Role hostTypeByName(final String name) {
        if (name == null) {
            return OTHER;
        }

        switch (name) {
            case HostTypes.SENDER_HOST_TYPE: {
                return SENDER;
            }
            case HostTypes.RECEIVER_HOST_TYPE: {
                return RECEIVER;
            }
            case HostTypes.INSPECTOR_HOST_TYPE: {
                return INSPECTOR;
            }
            case HostTypes.AGENT_HOST_TYPE: {
                return AGENT;
            }
            case HostTypes.EXPORTER_HOST_TYPE: {
                return EXPORTER;
            }
            default: {
                return OTHER;
            }
        }
    }
}
