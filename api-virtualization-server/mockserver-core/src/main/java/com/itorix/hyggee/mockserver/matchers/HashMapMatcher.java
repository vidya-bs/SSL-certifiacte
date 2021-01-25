package com.itorix.hyggee.mockserver.matchers;

import com.itorix.hyggee.mockserver.collections.CaseInsensitiveRegexHashMap;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.KeysAndValues;

/**
 *   
 */
public class HashMapMatcher extends NotMatcher<KeysAndValues> {

    private final MockServerLogger mockServerLogger;
    private final CaseInsensitiveRegexHashMap hashMap;

    public HashMapMatcher(MockServerLogger mockServerLogger, KeysAndValues keysAndValues) {
        this.mockServerLogger = mockServerLogger;
        if (keysAndValues != null) {
            this.hashMap = keysAndValues.toCaseInsensitiveRegexMultiMap();
        } else {
            this.hashMap = null;
        }
    }

    public boolean matches(final HttpRequest context, KeysAndValues values) {
        boolean result = false;

        if (hashMap == null || hashMap.isEmpty() || values == null) {
            result = true;
        } else if (values.toCaseInsensitiveRegexMultiMap().containsAll(hashMap)) {
            result = true;
        } else {
            mockServerLogger.trace(context, "Map [{}] is not a subset of {}", this.hashMap, values);
        }

        return not != result;
    }
}
