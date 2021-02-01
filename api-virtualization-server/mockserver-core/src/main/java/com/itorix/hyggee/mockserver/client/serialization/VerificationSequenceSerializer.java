package com.itorix.hyggee.mockserver.client.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.client.serialization.model.VerificationSequenceDTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.validator.jsonschema.JsonSchemaVerificationSequenceValidator;
import com.itorix.hyggee.mockserver.verify.VerificationSequence;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.VERIFICATION_FAILED;

/**
 *   
 */
public class VerificationSequenceSerializer implements Serializer<VerificationSequence> {
    private final MockServerLogger mockServerLogger;
    private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    private JsonSchemaVerificationSequenceValidator verificationSequenceValidator;

    public VerificationSequenceSerializer(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
        verificationSequenceValidator = new JsonSchemaVerificationSequenceValidator(mockServerLogger);
    }

    public String serialize(VerificationSequence verificationSequence) {
        try {
            return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(new VerificationSequenceDTO(verificationSequence));
        } catch (Exception e) {
            mockServerLogger.error("Exception while serializing verificationSequence to JSON with value " + verificationSequence, e);
            throw new RuntimeException("Exception while serializing verificationSequence to JSON with value " + verificationSequence, e);
        }
    }

    public VerificationSequence deserialize(String jsonVerificationSequence) {
        if (Strings.isNullOrEmpty(jsonVerificationSequence)) {
            throw new IllegalArgumentException("1 error:" + NEW_LINE + " - a verification sequence is required but value was \"" + String.valueOf(jsonVerificationSequence) + "\"");
        } else {
            String validationErrors = verificationSequenceValidator.isValid(jsonVerificationSequence);
            if (validationErrors.isEmpty()) {
                VerificationSequence verificationSequence = null;
                try {
                    VerificationSequenceDTO verificationDTO = objectMapper.readValue(jsonVerificationSequence, VerificationSequenceDTO.class);
                    if (verificationDTO != null) {
                        verificationSequence = verificationDTO.buildObject();
                    }
                } catch (Exception e) {
                    mockServerLogger.error((HttpRequest) null, e, "exception while parsing {}for VerificationSequence", jsonVerificationSequence);
                    throw new RuntimeException("Exception while parsing [" + jsonVerificationSequence + "] for VerificationSequence", e);
                }
                return verificationSequence;
            } else {
                mockServerLogger.debug(VERIFICATION_FAILED, "validation failed:{}verification sequence:{}", validationErrors, jsonVerificationSequence);
                throw new IllegalArgumentException(validationErrors);
            }
        }
    }

    @Override
    public Class<VerificationSequence> supportsType() {
        return VerificationSequence.class;
    }

}
