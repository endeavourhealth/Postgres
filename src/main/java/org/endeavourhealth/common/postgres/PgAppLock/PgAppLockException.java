package org.endeavourhealth.common.postgres.PgAppLock;

public class PgAppLockException extends Exception {
    static final long serialVersionUID = 0L;

    public PgAppLockException() {
        super();
    }
    public PgAppLockException(String message) {
        super(message);
    }
    public PgAppLockException(String message, Throwable cause) {
        super(message, cause);
    }
    public PgAppLockException(Throwable cause) {
        super(cause);
    }
}
