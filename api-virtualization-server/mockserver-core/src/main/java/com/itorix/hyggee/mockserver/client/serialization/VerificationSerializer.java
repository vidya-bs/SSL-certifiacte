package com.itorix.hyggee.mockserver.client.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.client.serialization.model.VerificationDTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.validator.jsonschema.JsonSchemaVerificationValidator;
import com.itorix.hyggee.mockserver.verify.Verification;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.VERIFICATION_FAILED;

/**
 *   
 */
public class VerificationSerializer implements Serializer<Verification> {
    private final MockServerLogger mockServerLogger;
    private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    private JsonSchemaVerificationValidator verificationValidator;

    public VerificationSerializer(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
        verificationValidator = new JsonSchemaVerificationValidator(mockServerLogger);
    }

    public String serialize(Verification verification) {
        try {
            return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(new VerificationDTO(verification));
        } catch (Exception e) {
            mockServerLogger.error("Exception while serializing verification to JSON with value " + verification, e);
            throw new RuntimeException("Exception while serializing verification to JSON with value " + verification, e);
        }
    }

    public Verification deserialize(String jsonVerification) {
        if (Strings.isNullOrEmpty(jsonVerification)) {
            throw new IllegalArgumentException("1 error:" + NEW_LINE + " - a verification is required but value was \"" + String.valueOf(jsonVerification) + "\"");
        } else {
            String validationErrors = verificationValidator.isValid(jsonVerification);
            if (validationErrors.isEmpty()) {
                Verification verification = null;
                try {
                    VerificationDTO verificationDTO = objectMapper.readValue(jsonVerification, VerificationDTO.class);
                    if (verificationDTO != null) {
                        verification = verificationDTO.buildObject();
                    }
                } catch (Exception e) {
                    mockServerLogger.error((HttpRequest) null, e, "exception while parsing {}for Verification", jsonVerification);
                    throw new RuntimeException("Exception while parsing [" + jsonVerification + "] for Verification", e);
                }
                return verification;
            } else {
                mockServerLogger.debug(VERIFICATION_FAILED, "validation failed:{}verification:{}", validationErrors, jsonVerification);
                throw new IllegalArgumentException(validationErrors);
            }
        }
    }

    @Override
    public Class<Verification> supportsType() {
        return Verification.class;
    }

}
