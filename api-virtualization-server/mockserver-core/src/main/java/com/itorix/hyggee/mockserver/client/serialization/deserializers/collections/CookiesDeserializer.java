package com.itorix.hyggee.mockserver.client.serialization.deserializers.collections;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.itorix.hyggee.mockserver.model.Cookies;
import com.itorix.hyggee.mockserver.model.NottableString;

import static com.itorix.hyggee.mockserver.model.NottableString.string;

import java.io.IOException;

/**
 *   
 */
public class CookiesDeserializer extends StdDeserializer<Cookies> {

    public CookiesDeserializer() {
        super(Cookies.class);
    }

    @Override
    public Cookies deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.isExpectedStartArrayToken()) {
            return deserializeArray(p, ctxt, ctxt.getNodeFactory());
        } else if (p.isExpectedStartObjectToken()) {
            return deserializeObject(p, ctxt, ctxt.getNodeFactory());
        } else {
            return (Cookies) ctxt.handleUnexpectedToken(Cookies.class, p);
        }
    }

    private Cookies deserializeObject(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        Cookies cookies = new Cookies();
        NottableString key = string("");
        while (true) {
            JsonToken t = p.nextToken();
            switch (t.id()) {
                case JsonTokenId.ID_FIELD_NAME:
                    key = string(ctxt.readValue(p, String.class));
                    break;
                case JsonTokenId.ID_STRING:
                    cookies.withEntry(key, string(ctxt.readValue(p, String.class)));
                    break;
                case JsonTokenId.ID_END_OBJECT:
                    return cookies;
                default:
                    throw new RuntimeException("Unexpected token: \"" + t + "\" id: \"" + t.id() + "\" text: \"" + p.getText());
            }
        }
    }

    private Cookies deserializeArray(JsonParser p, DeserializationContext ctxt, JsonNodeFactory nodeFactory) throws IOException {
        Cookies headers = new Cookies();
        NottableString key = null;
        NottableString value = null;
        String fieldName = null;
        while (true) {
            JsonToken t = p.nextToken();
            switch (t.id()) {
                case JsonTokenId.ID_END_ARRAY:
                    return headers;
                case JsonTokenId.ID_START_OBJECT:
                    key = null;
                    value = null;
                    break;
                case JsonTokenId.ID_FIELD_NAME:
                    fieldName = ctxt.readValue(p, String.class);
                    break;
                case JsonTokenId.ID_STRING:
                    if ("name".equals(fieldName)) {
                        key = string(ctxt.readValue(p, String.class));
                    } else if ("value".equals(fieldName)) {
                        value = string(ctxt.readValue(p, String.class));
                    }
                    break;
                case JsonTokenId.ID_END_OBJECT:
                    headers.withEntry(key, value);
                    break;
                default:
                    throw new RuntimeException("Unexpected token: \"" + t + "\" id: \"" + t.id() + "\" text: \"" + p.getText());
            }
        }
    }
}
