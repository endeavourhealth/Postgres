package org.endeavourhealth.common.postgres;

public class PgStoredProcException extends Exception {
    static final long serialVersionUID = 1L;

    public PgStoredProcException() {
        super();
    }
    public PgStoredProcException(String message) {
        super(message);
    }
    public PgStoredProcException(String message, Throwable cause) {
        super(message, cause);
    }
    public PgStoredProcException(Throwable cause) {
        super(cause);
    }
}
