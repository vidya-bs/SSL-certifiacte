package com.itorix.hyggee.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public class BooleanMatcher extends ObjectWithReflectiveEqualsHashCodeToString implements Matcher<Boolean> {
    private static final String[] excludedFields = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final Boolean matcher;

    public BooleanMatcher(MockServerLogger mockServerLogger, Boolean matcher) {
        this.mockServerLogger = mockServerLogger;
        this.matcher = matcher;
    }

    @Override
    public boolean matches(final HttpRequest context, Boolean matched) {
        boolean result = false;

        if (matcher == null) {
            result = true;
        } else if (matched != null) {
            result = matched == matcher;
        }

        if (!result) {
            mockServerLogger.trace(context, "Failed to match [{}] with [{}]", matched, this.matcher);
        }

        return result;
    }

    @Override
    @JsonIgnore
    public String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }

}
