package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDigestAsyncAppender extends AsyncAppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogDigestAsyncAppender.class);

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {
        LogDigestHelper.addLogAppender(new LogDigestAsyncAppender(digestLogger));
    }


    public LogDigestAsyncAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        super.append(eventObject);

        LogDigestHelper.saveDigestToDB(eventObject, dbLogger);
    }
}
