package com.itorix.hyggee.mockserver.model;

import com.google.common.net.MediaType;

import static com.itorix.hyggee.mockserver.mappers.ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET;

import java.nio.charset.Charset;

/**
 *   
 */
public class StringBody extends BodyWithContentType<String> {

    private final String value;
    private final byte[] rawBinaryData;
    private final boolean subString;

    public StringBody(String value) {
        this(value, false);
    }

    public StringBody(String value, Charset charset) {
        this(value, false, charset);
    }

    public StringBody(String value, MediaType contentType) {
        this(value, false, contentType);
    }

    public StringBody(String value, boolean subString) {
        this(value, subString, (MediaType) null);
    }

    public StringBody(String value, boolean subString, Charset charset) {
        this(value, subString, (charset != null ? MediaType.create("text", "plain").withCharset(charset) : null));
    }

    public StringBody(String value, boolean subString, MediaType contentType) {
        super(Type.STRING, contentType);
        this.value = value;
        this.subString = subString;

        if (value != null) {
            this.rawBinaryData = value.getBytes(determineCharacterSet(contentType, DEFAULT_HTTP_CHARACTER_SET));
        } else {
            this.rawBinaryData = new byte[0];
        }
    }

    public static StringBody exact(String body) {
        return new StringBody(body);
    }

    public static StringBody exact(String body, Charset charset) {
        return new StringBody(body, charset);
    }

    public static StringBody exact(String body, MediaType contentType) {
        return new StringBody(body, contentType);
    }

    public static StringBody subString(String body) {
        return new StringBody(body, true);
    }

    public static StringBody subString(String body, Charset charset) {
        return new StringBody(body, true, charset);
    }

    public static StringBody subString(String body, MediaType contentType) {
        return new StringBody(body, true, contentType);
    }

    public String getValue() {
        return value;
    }

    public byte[] getRawBytes() {
        return rawBinaryData;
    }

    public boolean isSubString() {
        return subString;
    }

    @Override
    public String toString() {
        return value;
    }
}
