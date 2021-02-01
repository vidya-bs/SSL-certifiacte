package com.itorix.hyggee.mockserver.matchers;

import static com.itorix.hyggee.mockserver.model.NottableString.string;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.NottableString;

/**
 *   
 */
public class ExactStringMatcher extends BodyMatcher<NottableString> {
    private static final String[] excludedFields = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final NottableString matcher;

    public ExactStringMatcher(MockServerLogger mockServerLogger, String matcher) {
        this.mockServerLogger = mockServerLogger;
        this.matcher = string(matcher);
    }

    public ExactStringMatcher(MockServerLogger mockServerLogger, NottableString matcher) {
        this.mockServerLogger = mockServerLogger;
        this.matcher = matcher;
    }

    public static boolean matches(String matcher, String matched, boolean ignoreCase) {
        boolean result = false;

        if (Strings.isNullOrEmpty(matcher)) {
            result = true;
        } else if (matched != null) {
            if (matched.equals(matcher)) {
                result = true;
            }
            // case insensitive comparison is mainly to improve matching in web containers like Tomcat that convert header names to lower case
            if (ignoreCase) {
                if (matched.equalsIgnoreCase(matcher)) {
                    result = true;
                }
            }
        }

        return result;
    }

    public boolean matches(final HttpRequest context, String matched) {
        return matches(context, string(matched));
    }

    public boolean matches(final HttpRequest context, NottableString matched) {
        boolean result = false;

        if (matches(matcher.getValue(), matched.getValue(), false)) {
            result = true;
        }

        if (!result) {
            mockServerLogger.trace(context, "Failed to match [{}] with [{}]", matched, this.matcher);
        }

        return matched.isNot() != (matcher.isNot() != (not != result));
    }

    @Override
    @JsonIgnore
    public String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }
}
