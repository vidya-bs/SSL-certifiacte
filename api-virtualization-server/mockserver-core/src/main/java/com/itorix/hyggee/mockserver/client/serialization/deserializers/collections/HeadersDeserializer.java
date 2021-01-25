package com.itorix.hyggee.mockserver.client.serialization.deserializers.collections;

import com.itorix.hyggee.mockserver.model.Headers;

/**
 *   
 */
public class HeadersDeserializer extends KeysToMultiValuesDeserializer<Headers> {

    public HeadersDeserializer() {
        super(Headers.class);
    }

    @Override
    public Headers build() {
        return new Headers();
    }
}
