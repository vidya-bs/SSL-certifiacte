package com.itorix.hyggee.mockserver.model;

import com.google.common.net.MediaType;
import com.itorix.hyggee.mockserver.client.serialization.Base64Converter;

/**
 *   
 */
public class BinaryBody extends BodyWithContentType<byte[]> {

    private final byte[] bytes;
    private final Base64Converter base64Converter = new Base64Converter();

    public BinaryBody(byte[] bytes) {
        this(bytes, null);
    }

    public BinaryBody(byte[] bytes, MediaType contentType) {
        super(Type.BINARY, contentType);
        this.bytes = bytes;
    }

    public static BinaryBody binary(byte[] body) {
        return new BinaryBody(body);
    }

    public static BinaryBody binary(byte[] body, MediaType contentType) {
        return new BinaryBody(body, contentType);
    }

    public byte[] getValue() {
        return bytes;
    }

    public byte[] getRawBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return bytes != null ? base64Converter.bytesToBase64String(bytes) : null;
    }
}
