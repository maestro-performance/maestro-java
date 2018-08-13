package org.maestro.common.io.data.common.exceptions;

import org.maestro.common.exceptions.MaestroException;

/**
 * Thrown if trying to save an invalid record
 */
public class InvalidRecordException extends MaestroException {
    public InvalidRecordException() {
    }

    public InvalidRecordException(String message) {
        super(message);
    }

    public InvalidRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRecordException(Throwable cause) {
        super(cause);
    }

    public InvalidRecordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
