package com.itorix.hyggee.mockserver.client.serialization;

/**
 *   
 */
public interface Serializer<T> {

    String serialize(T t);

    T deserialize(String json);

    Class<T> supportsType();
}
