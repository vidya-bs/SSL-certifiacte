package com.itorix.hyggee.mockserver.client.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.client.serialization.model.HttpResponseDTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.validator.jsonschema.JsonSchemaHttpResponseValidator;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *   
 */
public class HttpResponseSerializer implements Serializer<HttpResponse> {
    private final MockServerLogger mockServerLogger;
    private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    private JsonArraySerializer jsonArraySerializer = new JsonArraySerializer();
    private JsonSchemaHttpResponseValidator httpResponseValidator;

    public HttpResponseSerializer(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
        httpResponseValidator = new JsonSchemaHttpResponseValidator(mockServerLogger);
    }

    public String serialize(HttpResponse httpResponse) {
        try {
            return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(new HttpResponseDTO(httpResponse));
        } catch (Exception e) {
            mockServerLogger.error(String.format("Exception while serializing httpResponse to JSON with value %s", httpResponse), e);
            throw new RuntimeException(String.format("Exception while serializing httpResponse to JSON with value %s", httpResponse), e);
        }
    }

    public String serialize(List<HttpResponse> httpResponses) {
        return serialize(httpResponses.toArray(new HttpResponse[httpResponses.size()]));
    }

    public String serialize(HttpResponse... httpResponses) {
        try {
            if (httpResponses != null && httpResponses.length > 0) {
                HttpResponseDTO[] httpResponseDTOs = new HttpResponseDTO[httpResponses.length];
                for (int i = 0; i < httpResponses.length; i++) {
                    httpResponseDTOs[i] = new HttpResponseDTO(httpResponses[i]);
                }
                return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(httpResponseDTOs);
            } else {
                return "[]";
            }
        } catch (Exception e) {
            mockServerLogger.error("Exception while serializing HttpResponse to JSON with value " + Arrays.asList(httpResponses), e);
            throw new RuntimeException("Exception while serializing HttpResponse to JSON with value " + Arrays.asList(httpResponses), e);
        }
    }

    public HttpResponse deserialize(String jsonHttpResponse) {
        if (Strings.isNullOrEmpty(jsonHttpResponse)) {
            throw new IllegalArgumentException("1 error:" + NEW_LINE + " - a response is required but value was \"" + String.valueOf(jsonHttpResponse) + "\"");
        } else {
            if (jsonHttpResponse.contains("\"httpResponse\"")) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonHttpResponse);
                    if (jsonNode.has("httpResponse")) {
                        jsonHttpResponse = jsonNode.get("httpResponse").toString();
                    }
                } catch (Exception e) {
                    mockServerLogger.error((HttpRequest) null, e, "exception while parsing {}for HttpResponse", jsonHttpResponse);
                    throw new RuntimeException("Exception while parsing [" + jsonHttpResponse + "] for HttpResponse", e);
                }
            }
            String validationErrors = httpResponseValidator.isValid(jsonHttpResponse);
            if (validationErrors.isEmpty()) {
                HttpResponse httpResponse = null;
                try {
                    HttpResponseDTO httpResponseDTO = objectMapper.readValue(jsonHttpResponse, HttpResponseDTO.class);
                    if (httpResponseDTO != null) {
                        httpResponse = httpResponseDTO.buildObject();
                    }
                } catch (Exception e) {
                    mockServerLogger.error((HttpRequest) null, e, "exception while parsing {}for HttpResponse", jsonHttpResponse);
                    throw new RuntimeException("Exception while parsing [" + jsonHttpResponse + "] for HttpResponse", e);
                }
                return httpResponse;
            } else {
                mockServerLogger.error("validation failed:{}response:{}", validationErrors, jsonHttpResponse);
                throw new IllegalArgumentException(validationErrors);
            }
        }
    }

    @Override
    public Class<HttpResponse> supportsType() {
        return HttpResponse.class;
    }

    public HttpResponse[] deserializeArray(String jsonHttpResponses) {
        List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
        if (Strings.isNullOrEmpty(jsonHttpResponses)) {
            throw new IllegalArgumentException("1 error:" + NEW_LINE + " - a response or response array is required but value was \"" + String.valueOf(jsonHttpResponses) + "\"");
        } else {
            List<String> jsonResponseList = jsonArraySerializer.returnJSONObjects(jsonHttpResponses);
            if (jsonResponseList.isEmpty()) {
                throw new IllegalArgumentException("1 error:" + NEW_LINE + " - a response or array of response is required");
            } else {
                List<String> validationErrorsList = new ArrayList<String>();
                for (String jsonExpecation : jsonResponseList) {
                    try {
                        httpResponses.add(deserialize(jsonExpecation));
                    } catch (IllegalArgumentException iae) {
                        validationErrorsList.add(iae.getMessage());
                    }

                }
                if (!validationErrorsList.isEmpty()) {
                    throw new IllegalArgumentException((validationErrorsList.size() > 1 ? "[" : "") + Joiner.on("," + NEW_LINE).join(validationErrorsList) + (validationErrorsList.size() > 1 ? "]" : ""));
                }
            }
        }
        return httpResponses.toArray(new HttpResponse[httpResponses.size()]);
    }

}
