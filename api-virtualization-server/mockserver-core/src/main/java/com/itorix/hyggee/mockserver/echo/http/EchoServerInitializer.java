package com.itorix.hyggee.mockserver.echo.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import com.itorix.hyggee.mockserver.logging.LoggingHandler;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.server.netty.codec.MockServerServerCodec;

import static com.itorix.hyggee.mockserver.echo.http.EchoServer.*;
import static com.itorix.hyggee.mockserver.socket.NettySslContextFactory.nettySslContextFactory;
import static org.slf4j.event.Level.TRACE;

/**
 *   
 */
public class EchoServerInitializer extends ChannelInitializer<SocketChannel> {

    private final MockServerLogger mockServerLogger;
    private final boolean secure;
    private final EchoServer.Error error;

    public EchoServerInitializer(MockServerLogger mockServerLogger, boolean secure, EchoServer.Error error) {
        if (!secure && error == EchoServer.Error.CLOSE_CONNECTION) {
            throw new IllegalArgumentException("Error type CLOSE_CONNECTION is not supported in non-secure mode");
        }
        this.mockServerLogger = mockServerLogger;
        this.secure = secure;
        this.error = error;
    }

    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        if (error != null) {
            pipeline.addLast(new ErrorHandler(error));
        }

        if (secure) {
            pipeline.addLast(nettySslContextFactory().createServerSslContext().newHandler(channel.alloc()));
        }

        if (mockServerLogger.isEnabled(TRACE)) {
            pipeline.addLast(new LoggingHandler("EchoServer <-->"));
        }

        pipeline.addLast(new HttpServerCodec());

        pipeline.addLast(new HttpContentDecompressor());

        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));

        pipeline.addLast(new MockServerServerCodec(mockServerLogger, secure));

        if (!secure && error == EchoServer.Error.CLOSE_CONNECTION) {
            throw new IllegalArgumentException("Error type CLOSE_CONNECTION is not supported in non-secure mode");
        }

        pipeline.addLast(new EchoServerHandler(
            mockServerLogger,
            error,
            channel.attr(LOG_FILTER).get(),
            channel.attr(NEXT_RESPONSE).get(),
            channel.attr(ONLY_RESPONSE).get()
        ));
    }
}
