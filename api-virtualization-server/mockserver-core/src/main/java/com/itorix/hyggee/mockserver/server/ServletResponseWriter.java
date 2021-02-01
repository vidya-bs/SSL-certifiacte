package com.itorix.hyggee.mockserver.server;

import io.netty.handler.codec.http.HttpResponseStatus;

import com.itorix.hyggee.mockserver.cors.CORSHeaders;
import com.itorix.hyggee.mockserver.mappers.MockServerResponseToHttpServletResponseEncoder;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;

import javax.servlet.http.HttpServletResponse;

import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.enableCORSForAPI;
import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.enableCORSForAllResponses;
import static com.itorix.hyggee.mockserver.mock.HttpStateHandler.PATH_PREFIX;
import static com.itorix.hyggee.mockserver.model.Header.header;
import static com.itorix.hyggee.mockserver.model.HttpResponse.notFoundResponse;
import static com.itorix.hyggee.mockserver.model.HttpResponse.response;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 *   
 */
public class ServletResponseWriter extends ResponseWriter {
    private final HttpServletResponse httpServletResponse;
    private MockServerResponseToHttpServletResponseEncoder mockServerResponseToHttpServletResponseEncoder = new MockServerResponseToHttpServletResponseEncoder();
    private CORSHeaders addCORSHeaders = new CORSHeaders();

    public ServletResponseWriter(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public void writeResponse(HttpRequest request, HttpResponseStatus responseStatus) {
        writeResponse(request, responseStatus, "", "application/json");
    }

    @Override
    public void writeResponse(HttpRequest request, HttpResponseStatus responseStatus, String body, String contentType) {
        HttpResponse response = response()
            .withStatusCode(responseStatus.code())
            .withReasonPhrase(responseStatus.reasonPhrase())
            .withBody(body);
        if (body != null && !body.isEmpty()) {
            response.replaceHeader(header(CONTENT_TYPE.toString(), contentType + "; charset=utf-8"));
        }
        writeResponse(request, response, true);
    }

    @Override
    public void writeResponse(HttpRequest request, HttpResponse response, boolean apiResponse) {
        if (response == null) {
            response = notFoundResponse();
        }
        if (enableCORSForAllResponses()) {
            addCORSHeaders.addCORSHeaders(request, response);
        } else if (apiResponse && enableCORSForAPI()) {
            addCORSHeaders.addCORSHeaders(request, response);
        }
        if (apiResponse) {
//            response.withHeader("version", com.itorix.mockserver.Version.getVersion());
            final String path = request.getPath().getValue();
            if (!path.startsWith(PATH_PREFIX)) {
                response.withHeader("deprecated",
                    "\"" + path + "\" is deprecated use \"" + PATH_PREFIX + path + "\" instead");
            }
        }

        addConnectionHeader(request, response);

        mockServerResponseToHttpServletResponseEncoder.mapMockServerResponseToHttpServletResponse(response, httpServletResponse);
    }

}
