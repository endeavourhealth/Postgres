package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.endeavourhealth.common.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogDigestAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogDigestAppender.class);
    private static final String SKIP_LOG_DIGEST_MARKER = "SKIP_LOG_DIGEST_MARKER";

    private IDBDigestLogger dbLogger;

    public static void addLogAppender(IDBDigestLogger digestLogger) {

        // get the context
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

        // create the sync appender
        LogDigestAppender appender = new LogDigestAppender(digestLogger);
        appender.setContext(context);
        appender.setName(appender.getClass().getCanonicalName());
        appender.start();

        // add to the logger
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    public LogDigestAppender(IDBDigestLogger dbLogger) {
        this.dbLogger = dbLogger;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        saveDigestToDB(eventObject);
    }

    public void saveDigestToDB(ILoggingEvent eventObject) {
        if (eventObject == null)
            return;

        if (eventObject.getLevel() != Level.ERROR)
            return;

        if (eventObject.getMarker() != null)
            if (eventObject.getMarker().contains(SKIP_LOG_DIGEST_MARKER))
                return;

        try {
            String logClass = getLogClass(eventObject);
            String logMethod = getLogMethod(eventObject);
            String logMessage = eventObject.getFormattedMessage();
            String exception = getException(eventObject);

            dbLogger.logErrorDigest(logClass, logMethod, logMessage, exception);

        } catch (PgStoredProcException e) {

            Marker marker = MarkerFactory.getMarker(SKIP_LOG_DIGEST_MARKER);
            LOG.error(marker, "Error during logging error digest", e);
        }
    }

    public static String getException(ILoggingEvent eventObject) {
        return constructExceptionMessage(eventObject.getThrowableProxy());
    }

    public static String constructExceptionMessage(IThrowableProxy exception) {
        if (exception == null)
            return "";

        String message = "[" + exception.getClassName() + "]  " + exception.getMessage();

        if (exception.getCause() != null)
            if (exception.getCause() != exception)
                message += "\r\n" + constructExceptionMessage(exception.getCause());

        return message;
    }

    public static String getLogClass(ILoggingEvent eventObject) {
        StackTraceElement stackTraceElement = getFirstStackTraceElement(eventObject);

        if (stackTraceElement != null)
            return stackTraceElement.getClassName();

        return "";
    }

    public static String getLogMethod(ILoggingEvent eventObject) {
        StackTraceElement stackTraceElement = getFirstStackTraceElement(eventObject);

        if (stackTraceElement != null)
            return stackTraceElement.getMethodName();

        return "";
    }

    public static StackTraceElement getFirstStackTraceElement(ILoggingEvent eventObject) {
        if (eventObject.getCallerData() != null)
            if (eventObject.getCallerData().length > 0)
                if (eventObject.getCallerData()[0] != null)
                    return eventObject.getCallerData()[0];

        return null;
    }

}
