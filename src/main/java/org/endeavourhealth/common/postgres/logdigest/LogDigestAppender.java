package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDigestAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogDigestAppender.class);

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {
        LogDigestHelper.addLogAppender(new LogDigestAppender(digestLogger));
    }

    public LogDigestAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogDigestHelper.saveDigestToDB(eventObject, dbLogger);
    }
}
