package com.tianma.xsmscode.backup.exception;

/**
 * Version invalid exception
 */
public class VersionInvalidException extends BackupInvalidException {

    public VersionInvalidException() {
    }

    public VersionInvalidException(String message) {
        super(message);
    }

    public VersionInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionInvalidException(Throwable cause) {
        super(cause);
    }
}
