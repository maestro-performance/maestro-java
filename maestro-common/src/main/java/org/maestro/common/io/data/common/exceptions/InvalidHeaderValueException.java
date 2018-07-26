package org.maestro.common.io.data.common.exceptions;

import org.maestro.common.exceptions.MaestroException;

/**
 * Thrown if trying to set an invalid header value
 */
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
