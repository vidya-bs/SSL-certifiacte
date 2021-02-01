package com.itorix.hyggee.mockserver.client.serialization.serializers.collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.itorix.hyggee.mockserver.model.Cookie;
import com.itorix.hyggee.mockserver.model.Cookies;

import static com.itorix.hyggee.mockserver.model.NottableString.serialiseNottableString;

import java.io.IOException;

/**
 *   
 */
public class CookiesSerializer extends StdSerializer<Cookies> {

    public CookiesSerializer() {
        super(Cookies.class);
    }

    @Override
    public void serialize(Cookies collection, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        for (Cookie cookie : collection.getEntries()) {
            jgen.writeStringField(serialiseNottableString(cookie.getName()), serialiseNottableString(cookie.getValue()));
        }
        jgen.writeEndObject();
    }

}
