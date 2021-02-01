package com.itorix.hyggee.mockserver.validator.jsonschema;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

/**
 *   
 */
public class JsonSchemaHttpResponseValidator extends JsonSchemaValidator {

    public JsonSchemaHttpResponseValidator(MockServerLogger mockServerLogger) {
        super(
            mockServerLogger,
            "org/mockserver/model/schema/",
            "httpResponse",
            "bodyWithContentType",
            "delay",
            "connectionOptions",
            "keyToMultiValue",
            "keyToValue"
        );
    }

}
