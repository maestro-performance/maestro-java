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

package org.maestro.common.io.data.common.exceptions;

import org.maestro.common.exceptions.MaestroException;

/**
 * Thrown if trying to set an invalid header value
 */
@SuppressWarnings("serial")
public class InvalidHeaderValueException extends MaestroException {
    public InvalidHeaderValueException() {
    }

    public InvalidHeaderValueException(String message) {
        super(message);
    }

    public InvalidHeaderValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidHeaderValueException(Throwable cause) {
        super(cause);
    }

    public InvalidHeaderValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
