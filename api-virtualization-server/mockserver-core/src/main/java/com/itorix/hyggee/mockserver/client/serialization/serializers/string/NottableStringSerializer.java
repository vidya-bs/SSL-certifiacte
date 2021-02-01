package com.itorix.hyggee.mockserver.client.serialization.serializers.string;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.NottableString;

import static com.itorix.hyggee.mockserver.model.NottableString.serialiseNottableString;

import java.io.IOException;

/**
 *   
 */
public class NottableStringSerializer extends StdSerializer<NottableString> {

    public NottableStringSerializer() {
        super(NottableString.class);
    }

    @Override
    public void serialize(NottableString nottableString, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(serialiseNottableString(nottableString));
    }
}
