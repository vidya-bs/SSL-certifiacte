package com.itorix.hyggee.mockserver.client.netty.codec.mappers;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import com.itorix.hyggee.mockserver.mappers.ContentTypeMapper;
import com.itorix.hyggee.mockserver.model.*;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 *   
 */
public class FullHttpResponseToMockServerResponse {

    public HttpResponse mapMockServerResponseToFullHttpResponse(FullHttpResponse fullHttpResponse) {
        HttpResponse httpResponse = new HttpResponse();
        if (fullHttpResponse != null) {
            setStatusCode(httpResponse, fullHttpResponse);
            setHeaders(httpResponse, fullHttpResponse);
            setCookies(httpResponse);
            setBody(httpResponse, fullHttpResponse);
        }
        return httpResponse;
    }

    private void setStatusCode(HttpResponse httpResponse, FullHttpResponse fullHttpResponse) {
        HttpResponseStatus status = fullHttpResponse.status();
        httpResponse.withStatusCode(status.code());
        httpResponse.withReasonPhrase(status.reasonPhrase());
    }

    private void setHeaders(HttpResponse httpResponse, FullHttpResponse fullHttpResponse) {
        Headers headers = new Headers();
        for (String headerName : fullHttpResponse.headers().names()) {
            headers.withEntry(new Header(headerName, fullHttpResponse.headers().getAll(headerName)));
        }
        if (!headers.isEmpty()) {
            httpResponse.withHeaders(headers);
        }
    }

    private void setCookies(HttpResponse httpResponse) {
        Cookies cookies = new Cookies();
        for (Header header : httpResponse.getHeaderList()) {
            if (header.getName().getValue().equalsIgnoreCase("Set-Cookie")) {
                for (NottableString cookieHeader : header.getValues()) {
                    io.netty.handler.codec.http.cookie.Cookie httpCookie = ClientCookieDecoder.LAX.decode(cookieHeader.getValue());
                    String name = httpCookie.name().trim();
                    String value = httpCookie.value().trim();
                    cookies.withEntry(new Cookie(name, value));
                }
            }
            if (header.getName().getValue().equalsIgnoreCase("Cookie")) {
                for (NottableString cookieHeader : header.getValues()) {
                    for (io.netty.handler.codec.http.cookie.Cookie httpCookie : ServerCookieDecoder.LAX.decode(cookieHeader.getValue())) {
                        String name = httpCookie.name().trim();
                        String value = httpCookie.value().trim();
                        cookies.withEntry(new Cookie(name, value));
                    }
                }
            }
        }
        if (!cookies.isEmpty()) {
            httpResponse.withCookies(cookies);
        }
    }

    private void setBody(HttpResponse httpResponse, FullHttpResponse fullHttpResponse) {
        if (fullHttpResponse.content().readableBytes() > 0) {
            byte[] bodyBytes = new byte[fullHttpResponse.content().readableBytes()];
            fullHttpResponse.content().readBytes(bodyBytes);
            if (bodyBytes.length > 0) {
                if (ContentTypeMapper.isBinary(fullHttpResponse.headers().get(CONTENT_TYPE))) {
                    httpResponse.withBody(new BinaryBody(bodyBytes));
                } else {
                    Charset requestCharset = ContentTypeMapper.getCharsetFromContentTypeHeader(fullHttpResponse.headers().get(CONTENT_TYPE));
                    httpResponse.withBody(new String(bodyBytes, requestCharset));
                }
            }
        }
    }
}
