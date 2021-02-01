package com.itorix.hyggee.mockserver.client.serialization.serializers.collections;

import com.itorix.hyggee.mockserver.model.Headers;

/**
 *   
 */
public class HeadersSerializer extends KeysToMultiValuesSerializer<Headers> {

    public HeadersSerializer() {
        super(Headers.class);
    }

}
