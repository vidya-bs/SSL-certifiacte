package com.itorix.hyggee.mockserver.matchers;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public abstract class NotMatcher<MatchedType> extends ObjectWithReflectiveEqualsHashCodeToString implements Matcher<MatchedType> {

    boolean not = false;

    public static <MatcherType extends NotMatcher> MatcherType not(MatcherType matcher) {
        matcher.not = true;
        return matcher;
    }

}
