package com.itorix.hyggee.mockserver.client.serialization.deserializers.string;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.itorix.hyggee.mockserver.model.NottableString;

import org.apache.commons.lang3.StringUtils;

import static com.itorix.hyggee.mockserver.model.NottableString.string;

import java.io.IOException;

/**
 *   
 */
public class NottableStringDeserializer extends StdDeserializer<NottableString>  {
	
	 public NottableStringDeserializer() {
	        super(NottableString.class);
	    }

	
    @Override
    public NottableString deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            Boolean not = null;
            String string = null;

            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                if ("not".equals(fieldName)) {
                    jsonParser.nextToken();
                    not = jsonParser.getBooleanValue();
                } else if ("value".equals(fieldName)) {
                    jsonParser.nextToken();
                    string = ctxt.readValue(jsonParser, String.class);
                }
            }

            if (StringUtils.isEmpty(string)) {
                return null;
            }

            return string(string, not);
        } else if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING || jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
            return string(ctxt.readValue(jsonParser, String.class));
        }
        return null;
    }

}
