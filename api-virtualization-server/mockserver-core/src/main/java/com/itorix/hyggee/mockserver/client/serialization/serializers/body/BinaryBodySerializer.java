package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.BinaryBody;

import java.io.IOException;

/**
 *   
 */
public class BinaryBodySerializer extends StdSerializer<BinaryBody> {

    public BinaryBodySerializer() {
        super(BinaryBody.class);
    }

    @Override
    public void serialize(BinaryBody binaryBody, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (binaryBody.getContentType() != null) {
            jgen.writeStringField("contentType", binaryBody.getContentType());
        }
        jgen.writeStringField("type", binaryBody.getType().name());
        jgen.writeStringField("base64Bytes", binaryBody.toString());
        jgen.writeEndObject();
    }
}
