package org.endeavourhealth.common.postgres.logdigest;

import org.endeavourhealth.common.postgres.PgStoredProcException;

public interface IDBDigestLogger {
    void logErrorDigest(String logClass, String logMethod, String logMessage, String exception) throws Exception;
}
