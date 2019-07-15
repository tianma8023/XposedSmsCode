package com.tianma.xsmscode.feature.backup.exception;

/**
 * Backup invalid exception
 */
public class BackupInvalidException extends Exception {

    public BackupInvalidException() {
    }

    public BackupInvalidException(String message) {
        super(message);
    }

    public BackupInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackupInvalidException(Throwable cause) {
        super(cause);
    }
}
