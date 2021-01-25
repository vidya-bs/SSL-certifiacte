package com.itorix.hyggee.mockserver.client.serialization.serializers.collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.KeyToMultiValue;
import com.itorix.hyggee.mockserver.model.KeysToMultiValues;
import com.itorix.hyggee.mockserver.model.NottableString;

import static com.itorix.hyggee.mockserver.model.NottableString.serialiseNottableString;

import java.io.IOException;

/**
 *   
 */
public abstract class KeysToMultiValuesSerializer<T extends KeysToMultiValues<? extends KeyToMultiValue, T>> extends StdSerializer<T> {

    KeysToMultiValuesSerializer(Class<T> valueClass) {
        super(valueClass);
    }

    @Override
    public void serialize(T collection, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        for (KeyToMultiValue keyToMultiValue : collection.getEntries()) {
            jgen.writeFieldName(serialiseNottableString(keyToMultiValue.getName()));
            jgen.writeStartArray(keyToMultiValue.getValues().size());
            for (NottableString nottableString : keyToMultiValue.getValues()) {
                jgen.writeString(serialiseNottableString(nottableString));
            }
            jgen.writeEndArray();
        }
        jgen.writeEndObject();
    }

}
