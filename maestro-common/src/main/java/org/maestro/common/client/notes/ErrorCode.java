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

package org.maestro.common.client.notes;

/**
 * Error codes used in the InternalError note
 */
public enum ErrorCode {
    /**
     * Generic error
     */
    UNSPECIFIED(0),
    /**
     * Non-critical error whose condition or request can be retried at a later moment
     */
    TRY_AGAIN(10);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public static ErrorCode from(int value) {
        switch (value) {
            case 0: return UNSPECIFIED;
            case 10: return TRY_AGAIN;
            default: return UNSPECIFIED;
        }
    }

    public int getCode() {
        return code;
    }
}
