package com.itorix.hyggee.mockserver.client.serialization.java;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public interface ToJavaSerializer<T extends ObjectWithReflectiveEqualsHashCodeToString> {

    String serialize(int numberOfSpacesToIndent, T object);

}
