package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.XmlBodyDTO;
import com.itorix.hyggee.mockserver.model.XmlBody;

import java.io.IOException;

/**
 *   
 */
public class XmlBodyDTOSerializer extends StdSerializer<XmlBodyDTO> {

    public XmlBodyDTOSerializer() {
        super(XmlBodyDTO.class);
    }

    @Override
    public void serialize(XmlBodyDTO xmlBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (xmlBodyDTO.getNot() != null && xmlBodyDTO.getNot()) {
            jgen.writeBooleanField("not", true);
        }
        jgen.writeStringField("type", xmlBodyDTO.getType().name());
        jgen.writeStringField("xml", xmlBodyDTO.getXml());
        if (xmlBodyDTO.getContentType() != null && !xmlBodyDTO.getContentType().equals(XmlBody.DEFAULT_CONTENT_TYPE.toString())) {
            jgen.writeStringField("contentType", xmlBodyDTO.getContentType());
        }
        jgen.writeEndObject();
    }
}
