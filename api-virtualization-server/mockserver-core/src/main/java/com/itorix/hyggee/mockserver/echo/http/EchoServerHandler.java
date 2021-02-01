package com.itorix.hyggee.mockserver.echo.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;

import com.itorix.hyggee.mockserver.filters.MockServerEventLog;
import com.itorix.hyggee.mockserver.log.model.RequestLogEntry;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.BodyWithContentType;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.server.netty.codec.MockServerResponseEncoder;

import static com.itorix.hyggee.mockserver.model.HttpResponse.response;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 *   
 */
@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final MockServerLogger mockServerLogger;
    private final EchoServer.Error error;
    private final MockServerEventLog logFilter;
    private final EchoServer.NextResponse nextResponse;
    private final EchoServer.OnlyResponse onlyResponse;

    EchoServerHandler(MockServerLogger mockServerLogger, EchoServer.Error error, MockServerEventLog logFilter, EchoServer.NextResponse nextResponse, EchoServer.OnlyResponse onlyResponse) {
        this.mockServerLogger = mockServerLogger;
        this.error = error;
        this.logFilter = logFilter;
        this.nextResponse = nextResponse;
        this.onlyResponse = onlyResponse;
    }

    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {

        mockServerLogger.trace(request, "received request:{}", request);

        logFilter.add(new RequestLogEntry(request));

        if (onlyResponse.httpResponse != null) {
            // WARNING: this logic is only for unit tests that run in series and is NOT thread safe!!!
            DefaultFullHttpResponse httpResponse = new MockServerResponseEncoder().encode(onlyResponse.httpResponse);
            ctx.writeAndFlush(httpResponse);
        } else if (!nextResponse.httpResponse.isEmpty()) {
            // WARNING: this logic is only for unit tests that run in series and is NOT thread safe!!!
            DefaultFullHttpResponse httpResponse = new MockServerResponseEncoder().encode(nextResponse.httpResponse.remove());
            ctx.writeAndFlush(httpResponse);
        } else {
            HttpResponse httpResponse =
                response()
                    .withStatusCode(request.getPath().equalsIgnoreCase("/not_found") ? NOT_FOUND.code() : OK.code())
                    .withHeaders(request.getHeaderList());

            if (request.getBody() instanceof BodyWithContentType) {
                httpResponse.withBody((BodyWithContentType) request.getBody());
            } else {
                httpResponse.withBody(request.getBodyAsString());
            }

            // set hop-by-hop headers
            final int length = httpResponse.getBodyAsString() != null ? httpResponse.getBodyAsString().length() : 0;
            if (error == EchoServer.Error.LARGER_CONTENT_LENGTH) {
                httpResponse.replaceHeader(CONTENT_LENGTH.toString(), String.valueOf(length * 2));
            } else if (error == EchoServer.Error.SMALLER_CONTENT_LENGTH) {
                httpResponse.replaceHeader(CONTENT_LENGTH.toString(), String.valueOf(length / 2));
            } else {
                httpResponse.replaceHeader(CONTENT_LENGTH.toString(), String.valueOf(length));
            }

            // write and flush
            ctx.writeAndFlush(httpResponse);

            if (error == EchoServer.Error.LARGER_CONTENT_LENGTH || error == EchoServer.Error.SMALLER_CONTENT_LENGTH) {
                ctx.close();
            }
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
