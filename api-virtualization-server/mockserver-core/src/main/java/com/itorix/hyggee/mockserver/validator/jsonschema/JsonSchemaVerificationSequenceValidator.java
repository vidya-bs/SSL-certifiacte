package com.itorix.hyggee.mockserver.validator.jsonschema;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

/**
 *   
 */
public class JsonSchemaVerificationSequenceValidator extends JsonSchemaValidator {

    public JsonSchemaVerificationSequenceValidator(MockServerLogger mockServerLogger) {
        super(
            mockServerLogger,
            "org/mockserver/model/schema/",
            "verificationSequence",
            "httpRequest",
            "body",
            "keyToMultiValue",
            "keyToValue"
        );
    }
}
