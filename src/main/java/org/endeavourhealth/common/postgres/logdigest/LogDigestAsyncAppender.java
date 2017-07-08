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

        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

        LogDigestAsyncAppender asyncAppender = new LogDigestAsyncAppender();

        // create the actual appender
        LogDigestAppender appender = new LogDigestAppender(digestLogger);
        asyncAppender.addAppender(appender);

        asyncAppender.setContext(context);
        asyncAppender.setName(LogDigestAsyncAppender.class.getCanonicalName());
        asyncAppender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(asyncAppender);

    }

    public LogDigestAsyncAppender() {
        super();
    }
}
