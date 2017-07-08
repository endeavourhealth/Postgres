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

        // get the context
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

        // create the sync appender
        LogDigestAppender appender = new LogDigestAppender(digestLogger);
        appender.setContext(context);
        appender.setName(appender.getClass().getCanonicalName());
        appender.start();

        // create the async appender
        LogDigestAsyncAppender asyncAppender = new LogDigestAsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.setName(appender.getClass().getCanonicalName());
        asyncAppender.addAppender(appender);
        asyncAppender.start();

        // add to the logger
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(asyncAppender);
    }

    public LogDigestAsyncAppender() {
        super();
    }
}
