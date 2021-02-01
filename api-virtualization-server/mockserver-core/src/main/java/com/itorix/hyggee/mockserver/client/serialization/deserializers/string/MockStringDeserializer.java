package com.itorix.hyggee.mockserver.client.serialization.deserializers.string;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

public class MockStringDeserializer  extends StringDeserializer{
	 @Override
	    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
	    {
	        if (p.hasToken(JsonToken.VALUE_STRING)) {
	            return p.getText();
	        }
	        JsonToken t = p.currentToken();
	        // [databind#381]
	        if (t == JsonToken.START_ARRAY) {
	            return _deserializeFromArray(p, ctxt);
	        }
	        // need to gracefully handle byte[] data, as base64
	        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
	            Object ob = p.getEmbeddedObject();
	            if (ob == null) {
	                return null;
	            }
	            if (ob instanceof byte[]) {
	                return ctxt.getBase64Variant().encode((byte[]) ob, false);
	            }
	            // otherwise, try conversion using toString()...
	            return ob.toString();
	        }
	        // allow coercions for other scalar types
	        // 17-Jan-2018, tatu: Related to [databind#1853] avoid FIELD_NAME by ensuring it's
	        //   "real" scalar
//	        if (t.isScalarValue()) {
	            String text = p.getValueAsString();
	            if (text != null) {
	                return text;
	            }
//	        }
	        return (String) ctxt.handleUnexpectedToken(_valueClass, p);
	    }
}
