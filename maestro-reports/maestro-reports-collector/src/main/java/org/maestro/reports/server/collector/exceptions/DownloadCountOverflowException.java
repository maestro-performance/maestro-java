package org.maestro.reports.server.collector.exceptions;

import org.maestro.common.exceptions.MaestroException;

public class DownloadCountOverflowException extends MaestroException {
    public DownloadCountOverflowException(String message, Object... args) {
        super(message, args);
    }
}
