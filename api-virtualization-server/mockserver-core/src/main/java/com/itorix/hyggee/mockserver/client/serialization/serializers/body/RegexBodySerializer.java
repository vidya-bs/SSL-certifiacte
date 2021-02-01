package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.RegexBody;

import java.io.IOException;

/**
 *   
 */
public class RegexBodySerializer extends StdSerializer<RegexBody> {

    public RegexBodySerializer() {
        super(RegexBody.class);
    }

    @Override
    public void serialize(RegexBody regexBody, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (regexBody.getNot() != null && regexBody.getNot()) {
            jgen.writeBooleanField("not", regexBody.getNot());
        }
        jgen.writeStringField("type", regexBody.getType().name());
        jgen.writeStringField("regex", regexBody.getValue());
        jgen.writeEndObject();
    }
}
