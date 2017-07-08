package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;

public class LogDigestAsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {
        LogDigestHelper.addLogAppender(new LogDigestAsyncAppender(digestLogger));
    }

    public LogDigestAsyncAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogDigestHelper.saveDigestToDB(eventObject, dbLogger);
    }
}
