package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.JsonSchemaBodyDTO;

import java.io.IOException;

/**
 *   
 */
public class JsonSchemaBodyDTOSerializer extends StdSerializer<JsonSchemaBodyDTO> {

    public JsonSchemaBodyDTOSerializer() {
        super(JsonSchemaBodyDTO.class);
    }

    @Override
    public void serialize(JsonSchemaBodyDTO jsonSchemaBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (jsonSchemaBodyDTO.getNot() != null && jsonSchemaBodyDTO.getNot()) {
            jgen.writeBooleanField("not", jsonSchemaBodyDTO.getNot());
        }
        jgen.writeStringField("type", jsonSchemaBodyDTO.getType().name());
        jgen.writeStringField("jsonSchema", jsonSchemaBodyDTO.getJson());
        jgen.writeEndObject();
    }
}
