package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.JsonSchemaBody;

import java.io.IOException;

/**
 *   
 */
public class JsonSchemaBodySerializer extends StdSerializer<JsonSchemaBody> {

    public JsonSchemaBodySerializer() {
        super(JsonSchemaBody.class);
    }

    @Override
    public void serialize(JsonSchemaBody jsonSchemaBody, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (jsonSchemaBody.getNot() != null && jsonSchemaBody.getNot()) {
            jgen.writeBooleanField("not", jsonSchemaBody.getNot());
        }
        jgen.writeStringField("type", jsonSchemaBody.getType().name());
        jgen.writeStringField("jsonSchema", jsonSchemaBody.getValue());
        jgen.writeEndObject();
    }
}
