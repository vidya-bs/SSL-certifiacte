package com.itorix.hyggee.mockserver.client.serialization.java;

import java.util.List;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 *   
 */
public interface MultiValueToJavaSerializer<T extends ObjectWithReflectiveEqualsHashCodeToString> extends ToJavaSerializer<T> {

    public String serializeAsJava(int numberOfSpacesToIndent, List<T> object);

    public String serializeAsJava(int numberOfSpacesToIndent, T... object);

}
