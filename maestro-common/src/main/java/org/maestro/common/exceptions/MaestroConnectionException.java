package org.maestro.common.exceptions;

@SuppressWarnings({"unused", "serial"})
public class MaestroConnectionException extends MaestroException {
    public MaestroConnectionException() {
        super();
    }

    public MaestroConnectionException(String message) {
        super(message);
    }

    public MaestroConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaestroConnectionException(Throwable cause) {
        super(cause);
    }

    protected MaestroConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
