package com.itorix.hyggee.mockserver.validator.jsonschema;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

/**
 *   
 */
public class JsonSchemaExpectationValidator extends JsonSchemaValidator {

    public JsonSchemaExpectationValidator(MockServerLogger mockServerLogger) {
        super(mockServerLogger, "org/mockserver/model/schema/",
            "expectation",
            "httpRequest",
            "httpResponse",
            "httpTemplate",
            "httpForward",
            "httpClassCallback",
            "httpObjectCallback",
            "httpOverrideForwardedRequest",
            "httpError",
            "times",
            "timeToLive",
            "body",
            "bodyWithContentType",
            "delay",
            "connectionOptions",
            "keyToMultiValue",
            "keyToValue");
    }
}
