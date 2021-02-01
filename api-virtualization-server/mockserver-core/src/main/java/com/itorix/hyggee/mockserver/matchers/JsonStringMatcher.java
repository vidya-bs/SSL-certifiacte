package com.itorix.hyggee.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

/**
 *   
 */
public class JsonStringMatcher extends BodyMatcher<String> {
    private static final String[] excludedFields = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final String matcher;
    private final MatchType matchType;

    public JsonStringMatcher(MockServerLogger mockServerLogger, String matcher, MatchType matchType) {
        this.mockServerLogger = mockServerLogger;
        this.matcher = matcher;
        this.matchType = matchType;
    }

    public boolean matches(final HttpRequest context, String matched) {
        boolean result = false;

        JSONCompareResult jsonCompareResult;
        try {
            if (Strings.isNullOrEmpty(matcher)) {
                result = true;
            } else {
                JSONCompareMode jsonCompareMode = JSONCompareMode.LENIENT;
                if (matchType == MatchType.STRICT) {
                    jsonCompareMode = JSONCompareMode.STRICT;
                }
                jsonCompareResult = compareJSON(matcher, matched, jsonCompareMode);

                if (jsonCompareResult.passed()) {
                    result = true;
                }

                if (!result) {
                    mockServerLogger.trace(context, "Failed to perform JSON match \"{}\" with \"{}\" because {}", matched, this.matcher, jsonCompareResult.getMessage());
                }
            }
        } catch (Exception e) {
            mockServerLogger.trace(context, "Failed to perform JSON match \"{}\" with \"{}\" because {}", matched, this.matcher, e.getMessage());
        }

        return not != result;
    }

    @Override
    @JsonIgnore
    protected String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }
}
