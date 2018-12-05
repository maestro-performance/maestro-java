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

package org.maestro.common.agent;

/**
 * Abstraction for the user command data
 */
public class UserCommandData {
    private final long option;
    private final String payload;


    /**
     * Constructor
     * @param option An optional numeric option that can be associated w/ the command
     * @param payload An option string payload to be sent along w/ the command
     */
    public UserCommandData(long option, String payload) {
        this.option = option;
        this.payload = payload;
    }

    /**
     * Get the option value
     * @return the option value
     */
    public long getOption() {
        return option;
    }

    /**
     * Get the user command payload
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }
}
