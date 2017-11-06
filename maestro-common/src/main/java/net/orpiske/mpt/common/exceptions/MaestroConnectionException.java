package net.orpiske.mpt.common.exceptions;

public class MaestroConnectionException extends Exception {
    public MaestroConnectionException(String message) {
        super(message);
    }

    public MaestroConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
