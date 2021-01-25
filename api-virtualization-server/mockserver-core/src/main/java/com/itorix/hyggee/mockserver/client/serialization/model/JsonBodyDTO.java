package com.itorix.hyggee.mockserver.client.serialization.model;

import com.google.common.net.MediaType;
import com.itorix.hyggee.mockserver.matchers.MatchType;
import com.itorix.hyggee.mockserver.model.Body;
import com.itorix.hyggee.mockserver.model.JsonBody;

/**
 *   
 */
public class JsonBodyDTO extends BodyWithContentTypeDTO {

    private String json;
    private MatchType matchType;

    public JsonBodyDTO(JsonBody jsonBody) {
        this(jsonBody, false);
    }

    public JsonBodyDTO(JsonBody jsonBody, Boolean not) {
        super(Body.Type.JSON, not, jsonBody.getContentType());
        this.json = jsonBody.getValue();
        this.matchType = jsonBody.getMatchType();
    }

    public String getJson() {
        return json;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public JsonBody buildObject() {
        return new JsonBody(getJson(), (contentType != null ? MediaType.parse(contentType) : null), matchType);
    }
}
