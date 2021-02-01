package com.itorix.hyggee.mockserver.client.serialization.serializers.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.client.serialization.model.TimesDTO;
import com.itorix.hyggee.mockserver.matchers.Times;

import java.io.IOException;

/**
 *   
 */
public class TimesDTOSerializer extends StdSerializer<TimesDTO> {

    public TimesDTOSerializer() {
        super(TimesDTO.class);
    }

    @Override
    public void serialize(TimesDTO times, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (!times.isUnlimited()) {
            jgen.writeNumberField("remainingTimes", times.getRemainingTimes());
        } else {
            jgen.writeBooleanField("unlimited", times.isUnlimited());
        }
        jgen.writeEndObject();
    }
}
