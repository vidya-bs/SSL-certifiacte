package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.XmlSchemaBodyDTO;

import java.io.IOException;

/**
 *   
 */
public class XmlSchemaBodyDTOSerializer extends StdSerializer<XmlSchemaBodyDTO> {

    public XmlSchemaBodyDTOSerializer() {
        super(XmlSchemaBodyDTO.class);
    }

    @Override
    public void serialize(XmlSchemaBodyDTO xmlSchemaBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (xmlSchemaBodyDTO.getNot() != null && xmlSchemaBodyDTO.getNot()) {
            jgen.writeBooleanField("not", xmlSchemaBodyDTO.getNot());
        }
        jgen.writeStringField("type", xmlSchemaBodyDTO.getType().name());
        jgen.writeStringField("xmlSchema", xmlSchemaBodyDTO.getXml());
        jgen.writeEndObject();
    }
}
