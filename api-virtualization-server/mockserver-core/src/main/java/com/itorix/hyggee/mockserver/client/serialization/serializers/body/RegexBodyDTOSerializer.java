package com.itorix.hyggee.mockserver.client.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.RegexBodyDTO;

import java.io.IOException;

/**
 *   
 */
public class RegexBodyDTOSerializer extends StdSerializer<RegexBodyDTO> {

    public RegexBodyDTOSerializer() {
        super(RegexBodyDTO.class);
    }

    @Override
    public void serialize(RegexBodyDTO regexBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (regexBodyDTO.getNot() != null && regexBodyDTO.getNot()) {
            jgen.writeBooleanField("not", regexBodyDTO.getNot());
        }
        jgen.writeStringField("type", regexBodyDTO.getType().name());
        jgen.writeStringField("regex", regexBodyDTO.getRegex());
        jgen.writeEndObject();
    }
}
