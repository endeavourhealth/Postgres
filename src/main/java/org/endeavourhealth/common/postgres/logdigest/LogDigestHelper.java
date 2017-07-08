package org.endeavourhealth.common.postgres.logdigest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.Appender;
import org.endeavourhealth.common.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class LogDigestHelper {

    public static void addLogAppender(Appender<ILoggingEvent> appender) {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender.setContext(lc);
        appender.setName(appender.getClass().getCanonicalName());
        appender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    public static void saveDigestToDB(ILoggingEvent eventObject, IDBDigestLogger dbLogger) {
        if (eventObject == null)
            return;

        if (eventObject.getLevel() != Level.ERROR)
            return;

        try {
            String logClass = LogDigestHelper.getLogClass(eventObject);
            String logMethod = LogDigestHelper.getLogMethod(eventObject);
            String logMessage = eventObject.getFormattedMessage();
            String exception = LogDigestHelper.getException(eventObject);

            dbLogger.logErrorDigest(logClass, logMethod, logMessage, exception);
        } catch (PgStoredProcException e) {
            System.err.println("Error during logging error digest");
            System.err.println(e);
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
