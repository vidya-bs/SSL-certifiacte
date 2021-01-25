package com.itorix.hyggee.mockserver.mappers;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.itorix.hyggee.mockserver.model.*;
import com.itorix.hyggee.mockserver.streams.IOStreamUtils;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.itorix.hyggee.mockserver.mappers.ContentTypeMapper.DEFAULT_HTTP_CHARACTER_SET;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 *   
 */
@Component("httpServletRequestToMockServerRequestDecoder")
public class HttpServletRequestToMockServerRequestDecoder {
    public HttpRequest mapHttpServletRequestToMockServerRequest(HttpServletRequest httpServletRequest) {
        HttpRequest request = new HttpRequest();
        setMethod(request, httpServletRequest);

        setPath(request, httpServletRequest);
        setQueryString(request, httpServletRequest);

        setBody(request, httpServletRequest);
        setHeaders(request, httpServletRequest);
        setCookies(request, httpServletRequest);

        request.withKeepAlive(isKeepAlive(httpServletRequest));
        request.withSecure(httpServletRequest.isSecure());
        return request;
    }

    private void setMethod(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        httpRequest.withMethod(httpServletRequest.getMethod());
    }

    private void setPath(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        //httpRequest.withPath(httpServletRequest.getPathInfo() != null && httpServletRequest.getContextPath() != null ? httpServletRequest.getPathInfo() : httpServletRequest.getRequestURI());
        try {
			httpRequest.withPath(httpServletRequest.getPathInfo() != null && httpServletRequest.getContextPath() != null ? httpServletRequest.getPathInfo() : java.net.URLDecoder.decode(httpServletRequest.getRequestURI(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void setQueryString(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Parameters parameters = new Parameters();
        if (StringUtils.isNotEmpty(httpServletRequest.getQueryString())) {
            parameters.withEntries(new QueryStringDecoder("?" + httpServletRequest.getQueryString()).parameters());
        }
        httpRequest.withQueryStringParameters(parameters);
    }

    private void setBody(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        byte[] bodyBytes = IOStreamUtils.readInputStreamToByteArray(httpServletRequest);
        if (bodyBytes.length > 0) {
            if (ContentTypeMapper.isBinary(httpServletRequest.getHeader(CONTENT_TYPE.toString()))) {
                httpRequest.withBody(new BinaryBody(bodyBytes));
            } else {
                Charset requestCharset = ContentTypeMapper.getCharsetFromContentTypeHeader(httpServletRequest.getHeader(CONTENT_TYPE.toString()));
                httpRequest.withBody(new StringBody(new String(bodyBytes, requestCharset), DEFAULT_HTTP_CHARACTER_SET.equals(requestCharset) ? null : requestCharset));
            }
        }
    }

    private void setHeaders(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Headers headers = new Headers();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> mappedHeaderValues = new ArrayList<String>();
            Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                mappedHeaderValues.add(headerValues.nextElement());
            }
            headers.withEntry(new Header(headerName, mappedHeaderValues.toArray(new String[mappedHeaderValues.size()])));
        }
        httpRequest.withHeaders(headers);
    }

    private void setCookies(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Cookies cookies = new Cookies();
        if (httpServletRequest.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : httpServletRequest.getCookies()) {
                cookies.withEntry(new Cookie(cookie.getName(), cookie.getValue()));
            }
        }
        httpRequest.withCookies(cookies);
    }

    public boolean isKeepAlive(HttpServletRequest httpServletRequest) {
        CharSequence connection = httpServletRequest.getHeader(HttpHeaderNames.CONNECTION.toString());
        if (connection != null && HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
            return false;
        }

        if (httpServletRequest.getProtocol().equals("HTTP/1.1")) {
            return !HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection);
        } else {
            return HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connection);
        }
    }
}
