package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDigestAppender extends AppenderBase<ILoggingEvent> {

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {

        // create the sync appender
        LogDigestAppender appender = new LogDigestAppender(digestLogger);
        LogDigestHelper.configureAndStartAppender(appender);

        // add to the logger
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    public LogDigestAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogDigestHelper.saveDigestToDB(eventObject, dbLogger);
    }
}
