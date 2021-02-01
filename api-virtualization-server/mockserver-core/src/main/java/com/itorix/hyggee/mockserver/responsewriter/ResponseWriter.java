package com.itorix.hyggee.mockserver.responsewriter;

import io.netty.handler.codec.http.HttpResponseStatus;

import static com.itorix.hyggee.mockserver.model.ConnectionOptions.isFalseOrNull;
import static com.itorix.hyggee.mockserver.model.Header.header;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

import com.itorix.hyggee.mockserver.model.ConnectionOptions;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public abstract class ResponseWriter {

    public abstract void writeResponse(HttpRequest request, HttpResponseStatus responseStatus);

    public abstract void writeResponse(HttpRequest request, HttpResponseStatus responseStatus, String body, String contentType);

    public abstract void writeResponse(HttpRequest request, HttpResponse response, boolean apiResponse);

    protected void addConnectionHeader(HttpRequest request, HttpResponse response) {
        ConnectionOptions connectionOptions = response.getConnectionOptions();
        if (connectionOptions != null && connectionOptions.getKeepAliveOverride() != null) {
            if (connectionOptions.getKeepAliveOverride()) {
                response.replaceHeader(header(CONNECTION.toString(), KEEP_ALIVE.toString()));
            } else {
                response.replaceHeader(header(CONNECTION.toString(), CLOSE.toString()));
            }
        } else if (connectionOptions == null || isFalseOrNull(connectionOptions.getSuppressConnectionHeader())) {
            if (request.isKeepAlive() != null && request.isKeepAlive()
                && (connectionOptions == null || isFalseOrNull(connectionOptions.getCloseSocket()))) {
                response.replaceHeader(header(CONNECTION.toString(), KEEP_ALIVE.toString()));
            } else {
                response.replaceHeader(header(CONNECTION.toString(), CLOSE.toString()));
            }
        }
    }
}
