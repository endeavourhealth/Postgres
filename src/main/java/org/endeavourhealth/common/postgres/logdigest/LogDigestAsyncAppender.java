package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDigestAsyncAppender extends AsyncAppenderBase<ILoggingEvent> implements Appender<ILoggingEvent> {

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {
        LogDigestAsyncAppender logDigestAsyncAppender = new LogDigestAsyncAppender(digestLogger);
        logDigestAsyncAppender.addAppender(logDigestAsyncAppender);

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        logDigestAsyncAppender.setContext(lc);
        logDigestAsyncAppender.setName(LogDigestAsyncAppender.class.getCanonicalName());
        logDigestAsyncAppender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(logDigestAsyncAppender);

    }

    public LogDigestAsyncAppender(IDBDigestLogger dbLogger) {
        super();

        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogDigestHelper.saveDigestToDB(eventObject, dbLogger);
    }
}
