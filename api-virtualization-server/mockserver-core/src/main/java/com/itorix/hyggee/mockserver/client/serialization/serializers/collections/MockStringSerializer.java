package com.itorix.hyggee.mockserver.client.serialization.serializers.collections;

import java.io.IOException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

public class MockStringSerializer extends StdScalarSerializer<Object>{
	
	

	public MockStringSerializer() { super(String.class, false); }

	    @Override
	    public boolean isEmpty(SerializerProvider prov, Object value) {
	        String str = (String) value;
	        return str.length() == 0;
	    }

	    @Override
	    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
	        gen.writeString((String) value);
	    }

	    @Override
	    public final void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider,
	            TypeSerializer typeSer) throws IOException
	    {
	        // no type info, just regular serialization
	        gen.writeString((String) value);
	    }

	    @Override
	    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
	        return createSchemaNode("string", true);
	    }

	    @Override
	    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
	        visitStringFormat(visitor, typeHint);
	    }
}
