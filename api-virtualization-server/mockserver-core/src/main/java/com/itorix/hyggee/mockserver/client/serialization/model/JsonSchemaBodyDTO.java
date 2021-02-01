package com.itorix.hyggee.mockserver.client.serialization.model;

import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.JsonSchemaBody;

/**
 *   
 */
public class JsonSchemaBodyDTO extends BodyDTO {

    private String jsonSchema;

    public JsonSchemaBodyDTO(JsonSchemaBody jsonSchemaBody) {
        this(jsonSchemaBody, false);
    }

    public JsonSchemaBodyDTO(JsonSchemaBody jsonSchemaBody, Boolean not) {
        super(Body.Type.JSON_SCHEMA, not);
        this.jsonSchema = jsonSchemaBody.getValue();
    }

    public String getJson() {
        return jsonSchema;
    }

    public JsonSchemaBody buildObject() {
        return new JsonSchemaBody(getJson());
    }
}
