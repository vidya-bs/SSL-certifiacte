package com.itorix.hyggee.mockserver.logging;

import com.google.common.collect.ImmutableList;
import com.itorix.hyggee.mockserver.configuration.ConfigurationProperties;
import com.itorix.hyggee.mockserver.log.model.MessageLogEntry;
import com.itorix.hyggee.mockserver.mock.HttpStateHandler;
import com.itorix.hyggee.mockserver.model.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.DEFAULT_LOG_LEVEL;
import static com.itorix.hyggee.mockserver.formatting.StringFormatter.formatLogMessage;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.EXCEPTION;
import static com.itorix.hyggee.mockserver.model.HttpRequest.request;
import static org.slf4j.event.Level.*;

/**
 *   
 */
@Component("mockServerLogger")
public class MockServerLogger {

    public static final MockServerLogger MOCK_SERVER_LOGGER = new MockServerLogger();

    static {
        setRootLogLevel("io.netty");
        setRootLogLevel("org.apache.velocity");
        Logger mockServerLogger = LoggerFactory.getLogger("org.mockserver");
        if (mockServerLogger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) mockServerLogger).setLevel(
                ch.qos.logback.classic.Level.valueOf(System.getProperty("mockserver.logLevel", DEFAULT_LOG_LEVEL))
            );
        }
    }

    private final boolean auditEnabled = !ConfigurationProperties.disableRequestAudit();
    private final boolean logEnabled = !ConfigurationProperties.disableSystemOut();
    private final Logger logger;
    private final HttpStateHandler httpStateHandler;

    public MockServerLogger() {
        this(MockServerLogger.class);
    }

    public MockServerLogger(final Class loggerClass) {
        this(LoggerFactory.getLogger(loggerClass), null);
    }

    public MockServerLogger(final Logger logger, final @Nullable HttpStateHandler httpStateHandler) {
        this.logger = logger;
        this.httpStateHandler = httpStateHandler;
    }

    public static void setRootLogLevel(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(
                ch.qos.logback.classic.Level.valueOf(System.getProperty("root.logLevel", "WARN"))
            );
        }
    }

    public void trace(final String message, final Object... arguments) {
        trace(null, message, arguments);
    }

    public void trace(final HttpRequest request, final String message, final Object... arguments) {
        if (isEnabled(TRACE)) {
            addLogEvents(MessageLogEntry.LogMessageType.TRACE, TRACE, request, message, arguments);
            final String logMessage = formatLogMessage(message, arguments);
            if (logEnabled) {
                logger.trace(logMessage);
            }
        }
    }

    public void debug(final MessageLogEntry.LogMessageType type, final String message, final Object... arguments) {
        debug(type, null, message, arguments);
    }

    public void debug(final MessageLogEntry.LogMessageType type, final HttpRequest request, final String message, final Object... arguments) {
        if (isEnabled(DEBUG)) {
            addLogEvents(type, DEBUG, request, message, arguments);
            final String logMessage = formatLogMessage(message, arguments);
            if (logEnabled) {
                logger.debug(logMessage);
            }
        }
    }

    public void info(final MessageLogEntry.LogMessageType type, final String message, final Object... arguments) {
        info(type, (HttpRequest) null, message, arguments);
    }

    public void info(Map logMap){
    	StringBuffer logString = new StringBuffer();
		logString.append("date=" + logMap.get("date") + "||");
		logMap.remove("date");
		logString.append("guid=" + logMap.get("guid") + "||");
		logMap.remove("guid");
		logString.append("clientIp=" + logMap.get("clientIp") + "||");
		logMap.remove("clientIp");
		logString.append("path=" + logMap.get("path") + "||");
		logMap.remove("path");
		logString.append("logMessage=" + logMap.get("logMessage") + "||");
		logMap.remove("logMessage");
    	if (logEnabled) {
            logger.info(logString.toString());
        }
    }
    
    public void info(final MessageLogEntry.LogMessageType type, final HttpRequest request, final String message, final Object... arguments) {
        info(type, ImmutableList.of(request != null ? request : request()), message, arguments);
    }

    public void info(final MessageLogEntry.LogMessageType type, final List<HttpRequest> requests, final String message, final Object... arguments) {
        if (isEnabled(INFO)) {
            addLogEvents(type, INFO, requests, message, arguments);
            final String logMessage = formatLogMessage(message, arguments);
            if (logEnabled) {
                logger.debug(logMessage);
            }
        }
    }

    public void warn(final String message) {
        warn((HttpRequest) null, message);
    }

    public void warn(final String message, final Object... arguments) {
        warn(null, message, arguments);
    }

    public void warn(final @Nullable HttpRequest request, final String message, final Object... arguments) {
        if (isEnabled(WARN)) {
            addLogEvents(MessageLogEntry.LogMessageType.WARN, WARN, request, message, arguments);
            final String logMessage = formatLogMessage(message, arguments);
            if (logEnabled) {
                logger.error(logMessage);
            }
        }
    }

    public void error(final String message, final Throwable throwable) {
        error((HttpRequest) null, throwable, message);
    }

    public void error(final String message, final Object... arguments) {
        error(null, message, arguments);
    }

    public void error(final @Nullable HttpRequest request, final String message, final Object... arguments) {
        error(request, null, message, arguments);
    }

    public void error(final @Nullable HttpRequest request, final Throwable throwable, final String message, final Object... arguments) {
        error(ImmutableList.of(request != null ? request : request()), throwable, message, arguments);
    }

    public void error(final List<HttpRequest> requests, final Throwable throwable, final String message, final Object... arguments) {
        if (isEnabled(ERROR)) {
            addLogEvents(EXCEPTION, ERROR, requests, message, arguments);
            final String logMessage = formatLogMessage(message, arguments);
            if (logEnabled) {
                logger.error(logMessage, throwable);
            }
        }
    }

    private void addLogEvents(final MessageLogEntry.LogMessageType type, final Level logLeveL, final @Nullable HttpRequest request, final String message, final Object... arguments) {
        if (auditEnabled && httpStateHandler != null) {
            httpStateHandler.log(new MessageLogEntry(type, logLeveL, request, message, arguments));
        }
    }

    private void addLogEvents(final MessageLogEntry.LogMessageType type, final Level logLeveL, final List<HttpRequest> requests, final String message, final Object... arguments) {
        if (auditEnabled && httpStateHandler != null) {
            httpStateHandler.log(new MessageLogEntry(type, logLeveL, requests, message, arguments));
        }
    }

    public boolean isEnabled(final Level level) {
        return level.toInt() >= ConfigurationProperties.logLevel().toInt();
    }
}
