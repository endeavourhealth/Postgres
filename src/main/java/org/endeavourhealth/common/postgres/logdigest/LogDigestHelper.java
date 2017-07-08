package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.Appender;
import org.endeavourhealth.common.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

abstract class LogDigestHelper {

    private static final Logger LOG = LoggerFactory.getLogger(LogDigestHelper.class);
    public static final String SKIP_LOG_DIGEST_MARKER = "SKIP_LOG_DIGEST_MARKER";

    public static void configureAndStartAppender(Appender<ILoggingEvent> appender) {
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

        appender.setContext(context);
        appender.setName(appender.getClass().getCanonicalName());
        appender.start();
    }

    public static void saveDigestToDB(ILoggingEvent eventObject, IDBDigestLogger dbLogger) {
        if (eventObject == null)
            return;

        if (eventObject.getLevel() != Level.ERROR)
            return;

        if (eventObject.getMarker() != null)
            if (eventObject.getMarker().contains(SKIP_LOG_DIGEST_MARKER))
                return;

        try {
            String logClass = LogDigestHelper.getLogClass(eventObject);
            String logMethod = LogDigestHelper.getLogMethod(eventObject);
            String logMessage = eventObject.getFormattedMessage();
            String exception = LogDigestHelper.getException(eventObject);

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
