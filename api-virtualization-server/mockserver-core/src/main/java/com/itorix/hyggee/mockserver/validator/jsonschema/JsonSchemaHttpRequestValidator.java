package com.itorix.hyggee.mockserver.validator.jsonschema;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

/**
 *   
 */
public class JsonSchemaHttpRequestValidator extends JsonSchemaValidator {

    public JsonSchemaHttpRequestValidator(MockServerLogger mockServerLogger) {
        super(mockServerLogger,
            "org/mockserver/model/schema/",
            "httpRequest",
            "body",
            "keyToMultiValue",
            "keyToValue");
    }
}
