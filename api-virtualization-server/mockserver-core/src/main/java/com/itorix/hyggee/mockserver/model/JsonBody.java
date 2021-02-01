package com.itorix.hyggee.mockserver.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.matchers.MatchType;

import static com.itorix.hyggee.mockserver.mappers.ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET;

import java.nio.charset.Charset;

/**
 *   
 */
public class JsonBody extends BodyWithContentType {

    public static final MatchType DEFAULT_MATCH_TYPE = MatchType.ONLY_MATCHING_FIELDS;
    public static final MediaType DEFAULT_CONTENT_TYPE = MediaType.create("application", "json");
    private final String json;
    private final byte[] rawBinaryData;
    private final MatchType matchType;

    public JsonBody(String json) {
        this(json, DEFAULT_CONTENT_TYPE, DEFAULT_MATCH_TYPE);
    }

    public JsonBody(String json, MatchType matchType) {
        this(json, MediaType.create("application", "json"), matchType);
    }

    public JsonBody(String json, Charset charset, MatchType matchType) {
        this(json, (charset != null ? MediaType.create("application", "json").withCharset(charset) : null), matchType);
    }

    public JsonBody(String json, MediaType contentType, MatchType matchType) {
        super(Type.JSON, contentType);
        this.json = json;
        this.matchType = matchType;

        if (json != null) {
            this.rawBinaryData = json.getBytes(determineCharacterSet(contentType, DEFAULT_HTTP_CHARACTER_SET));
        } else {
            this.rawBinaryData = new byte[0];
        }
    }

    public static JsonBody json(String json) {
        return new JsonBody(json);
    }

    public static JsonBody json(String json, MatchType matchType) {
        return new JsonBody(json, matchType);
    }

    public static JsonBody json(String json, Charset charset) {
        return new JsonBody(json, charset, DEFAULT_MATCH_TYPE);
    }

    public static JsonBody json(String json, Charset charset, MatchType matchType) {
        return new JsonBody(json, charset, matchType);
    }

    public static JsonBody json(String json, MediaType contentType) {
        return new JsonBody(json, contentType, DEFAULT_MATCH_TYPE);
    }

    public static JsonBody json(String json, MediaType contentType, MatchType matchType) {
        return new JsonBody(json, contentType, matchType);
    }

    private static String toJson(Object object) {
        String json = "";
        try {
            json = ObjectMapperFactory.createObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            new MockServerLogger(JsonBody.class).error("error mapping object for json body to JSON", e);
        }
        return json;
    }

    public static JsonBody json(Object object) {
        return new JsonBody(toJson(object));
    }

    public static JsonBody json(Object object, MatchType matchType) {
        return new JsonBody(toJson(object), matchType);
    }

    public static JsonBody json(Object object, Charset charset) {
        return new JsonBody(toJson(object), charset, DEFAULT_MATCH_TYPE);
    }

    public static JsonBody json(Object object, Charset charset, MatchType matchType) {
        return new JsonBody(toJson(object), charset, matchType);
    }

    public static JsonBody json(Object object, MediaType contentType) {
        return new JsonBody(toJson(object), contentType, DEFAULT_MATCH_TYPE);
    }

    public static JsonBody json(Object object, MediaType contentType, MatchType matchType) {
        return new JsonBody(toJson(object), contentType, matchType);
    }

    public String getValue() {
        return json;
    }

    public byte[] getRawBytes() {
        return rawBinaryData;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    @Override
    public String toString() {
        return json;
    }

}
