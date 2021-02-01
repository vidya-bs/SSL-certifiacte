package com.itorix.hyggee.mockserver.client.serialization;

import javax.xml.bind.DatatypeConverter;

import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *   
 */
public class Base64Converter extends ObjectWithReflectiveEqualsHashCodeToString {

    private static final String BASE64_PATTERN = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

    public byte[] base64StringToBytes(String data) {
        if (data == null) {
            return new byte[0];
        }
        if (!data.matches(BASE64_PATTERN)) {
            return data.getBytes(UTF_8);
        }
        return DatatypeConverter.parseBase64Binary(data);
    }

    public String bytesToBase64String(byte[] data) {
        return DatatypeConverter.printBase64Binary(data);
    }

}
