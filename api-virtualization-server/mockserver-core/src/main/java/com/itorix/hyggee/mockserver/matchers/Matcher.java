package com.itorix.hyggee.mockserver.matchers;

import com.itorix.hyggee.mockserver.model.HttpRequest;

/**
 *   
 */
public interface Matcher<T> {

    boolean matches(HttpRequest context, T t);
}
