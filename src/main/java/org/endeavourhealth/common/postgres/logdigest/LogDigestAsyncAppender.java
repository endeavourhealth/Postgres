package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDigestAsyncAppender extends AsyncAppenderBase<ILoggingEvent> implements Appender<ILoggingEvent> {

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {

        // create the sync appender
        LogDigestAppender appender = new LogDigestAppender(digestLogger);
        LogDigestHelper.configureAndStartAppender(appender);

        // create the async appender
        LogDigestAsyncAppender asyncAppender = new LogDigestAsyncAppender();
        asyncAppender.addAppender(appender);
        LogDigestHelper.configureAndStartAppender(asyncAppender);

        // add to the logger
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(asyncAppender);
    }

    public LogDigestAsyncAppender() {
        super();
    }
}
