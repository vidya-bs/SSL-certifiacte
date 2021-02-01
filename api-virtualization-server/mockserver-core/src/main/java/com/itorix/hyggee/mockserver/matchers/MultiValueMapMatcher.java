package com.itorix.hyggee.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.hyggee.mockserver.collections.CaseInsensitiveRegexMultiMap;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.KeysToMultiValues;

/**
 *   
 */
public class MultiValueMapMatcher extends NotMatcher<KeysToMultiValues> {
    private static final String[] excludedFields = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final CaseInsensitiveRegexMultiMap multiMap;

    public MultiValueMapMatcher(MockServerLogger mockServerLogger, KeysToMultiValues keysToMultiValues) {
        this.mockServerLogger = mockServerLogger;
        if (keysToMultiValues != null) {
            this.multiMap = keysToMultiValues.toCaseInsensitiveRegexMultiMap();
        } else {
            this.multiMap = null;
        }
    }

    public boolean matches(final HttpRequest context, KeysToMultiValues values) {
        boolean result = false;

        if (multiMap == null || multiMap.isEmpty()) {
            result = true;
        } else if (values.toCaseInsensitiveRegexMultiMap().containsAll(multiMap)) {
            result = true;
        } else {
            mockServerLogger.trace(context, "Map [{}] is not a subset of {}", multiMap, values);
        }

        return not != result;
    }

    @Override
    @JsonIgnore
    protected String[] fieldsExcludedFromEqualsAndHashCode() {
        return excludedFields;
    }
}
