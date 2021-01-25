package com.itorix.hyggee.mockserver.client.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.itorix.hyggee.mockserver.client.serialization.model.WebSocketMessageDTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 *   
 */
public class WebSocketMessageSerializer {

    private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    private Map<Class, Serializer> serializers;

    public WebSocketMessageSerializer(MockServerLogger mockServerLogger) {
        serializers = ImmutableMap.<Class, Serializer>of(
            HttpRequest.class, new HttpRequestSerializer(mockServerLogger),
            HttpResponse.class, new HttpResponseSerializer(mockServerLogger)
        );
    }

    public String serialize(Object message) throws JsonProcessingException {
        if (serializers.containsKey(message.getClass())) {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new WebSocketMessageDTO().setType(message.getClass().getName()).setValue(serializers.get(message.getClass()).serialize((message))));
        } else {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new WebSocketMessageDTO().setType(message.getClass().getName()).setValue(objectMapper.writeValueAsString(message)));
        }
    }

    public Object deserialize(String messageJson) throws ClassNotFoundException, IOException {
        WebSocketMessageDTO webSocketMessageDTO = objectMapper.readValue(messageJson, WebSocketMessageDTO.class);
        if (webSocketMessageDTO.getType() != null && webSocketMessageDTO.getValue() != null) {
            Class format = Class.forName(webSocketMessageDTO.getType());
            if (serializers.containsKey(format)) {
                return serializers.get(format).deserialize(webSocketMessageDTO.getValue());
            } else {
                return objectMapper.readValue(webSocketMessageDTO.getValue(), format);
            }
        } else {
            return null;
        }
    }
}
