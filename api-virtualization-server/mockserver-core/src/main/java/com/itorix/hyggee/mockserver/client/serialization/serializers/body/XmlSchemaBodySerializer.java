package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.XmlSchemaBody;

import java.io.IOException;

/**
 *   
 */
public class XmlSchemaBodySerializer extends StdSerializer<XmlSchemaBody> {

    public XmlSchemaBodySerializer() {
        super(XmlSchemaBody.class);
    }

    @Override
    public void serialize(XmlSchemaBody xmlSchemaBody, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (xmlSchemaBody.getNot() != null && xmlSchemaBody.getNot()) {
            jgen.writeBooleanField("not", xmlSchemaBody.getNot());
        }
        jgen.writeStringField("type", xmlSchemaBody.getType().name());
        jgen.writeStringField("xmlSchema", xmlSchemaBody.getValue());
        jgen.writeEndObject();
    }
}
