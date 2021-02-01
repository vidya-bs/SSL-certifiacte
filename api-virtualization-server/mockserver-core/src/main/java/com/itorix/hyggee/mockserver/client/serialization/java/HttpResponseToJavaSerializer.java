package com.itorix.hyggee.mockserver.client.serialization.java;

import com.google.common.base.Strings;
import com.itorix.hyggee.mockserver.client.serialization.Base64Converter;
import com.itorix.hyggee.mockserver.model.*;

import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.client.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 *   
 */
public class HttpResponseToJavaSerializer implements ToJavaSerializer<HttpResponse> {

    private final Base64Converter base64Converter = new Base64Converter();

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpResponse httpResponse) {
        StringBuffer output = new StringBuffer();
        if (httpResponse != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("response()");
            if (httpResponse.getStatusCode() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withStatusCode(").append(httpResponse.getStatusCode()).append(")");
            }
            if (httpResponse.getReasonPhrase() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withReasonPhrase(\"").append(StringEscapeUtils.escapeJava(httpResponse.getReasonPhrase())).append("\")");
            }
            outputHeaders(numberOfSpacesToIndent + 1, output, httpResponse.getHeaderList());
            outputCookies(numberOfSpacesToIndent + 1, output, httpResponse.getCookieList());
            if (!Strings.isNullOrEmpty(httpResponse.getBodyAsString())) {
                if (httpResponse.getBody() instanceof BinaryBody) {
                    appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output);
                    BinaryBody body = (BinaryBody) httpResponse.getBody();
                    output.append(".withBody(new Base64Converter().base64StringToBytes(\"").append(base64Converter.bytesToBase64String(body.getRawBytes())).append("\"))");
                } else {
                    appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withBody(\"").append(StringEscapeUtils.escapeJava(httpResponse.getBodyAsString())).append("\")");
                }
            }
            if (httpResponse.getDelay() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withDelay(").append(new DelayToJavaSerializer().serialize(0, httpResponse.getDelay())).append(")");
            }
            if (httpResponse.getConnectionOptions() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withConnectionOptions(");
                output.append(new ConnectionOptionsToJavaSerializer().serialize(numberOfSpacesToIndent + 2, httpResponse.getConnectionOptions()));
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(")");
            }
        }

        return output.toString();
    }

    private void outputCookies(int numberOfSpacesToIndent, StringBuffer output, List<Cookie> cookies) {
        if (cookies.size() > 0) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append(".withCookies(");
            appendObject(numberOfSpacesToIndent + 1, output, new CookieToJavaSerializer(), cookies);
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append(")");
        }
    }

    private void outputHeaders(int numberOfSpacesToIndent, StringBuffer output, List<Header> headers) {
        if (headers.size() > 0) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append(".withHeaders(");
            appendObject(numberOfSpacesToIndent + 1, output, new HeaderToJavaSerializer(), headers);
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append(")");
        }
    }

    private <T extends ObjectWithReflectiveEqualsHashCodeToString> void appendObject(int numberOfSpacesToIndent, StringBuffer output, MultiValueToJavaSerializer<T> toJavaSerializer, List<T> objects) {
        output.append(toJavaSerializer.serializeAsJava(numberOfSpacesToIndent, objects));
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
