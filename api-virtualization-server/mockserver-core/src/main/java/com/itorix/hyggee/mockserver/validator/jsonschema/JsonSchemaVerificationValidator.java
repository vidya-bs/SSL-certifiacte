package com.itorix.hyggee.mockserver.validator.jsonschema;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

/**
 *   
 */
public class JsonSchemaVerificationValidator extends JsonSchemaValidator {

    public JsonSchemaVerificationValidator(MockServerLogger mockServerLogger) {
        super(
            mockServerLogger,
            "org/mockserver/model/schema/",
            "verification",
            "httpRequest",
            "body",
            "keyToMultiValue",
            "keyToValue",
            "verificationTimes"
        );
    }

}
