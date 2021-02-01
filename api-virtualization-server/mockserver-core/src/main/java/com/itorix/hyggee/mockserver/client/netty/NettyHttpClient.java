package com.itorix.hyggee.mockserver.client.netty;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.proxy.ProxyConfiguration;
import com.itorix.hyggee.mockserver.client.netty.proxy.Type;
import com.itorix.hyggee.mockserver.configuration.ConfigurationProperties;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyHttpClient {

    static final MockServerLogger mockServerLogger = new MockServerLogger(NettyHttpClient.class);
    static final AttributeKey<Boolean> SECURE = AttributeKey.valueOf("SECURE");
    static final AttributeKey<InetSocketAddress> REMOTE_SOCKET = AttributeKey.valueOf("REMOTE_SOCKET");
    static final AttributeKey<HttpRequest> REQUEST = AttributeKey.valueOf("REQUEST");
    static final AttributeKey<SettableFuture<HttpResponse>> RESPONSE_FUTURE = AttributeKey.valueOf("RESPONSE_FUTURE");
    private static EventLoopGroup group = new NioEventLoopGroup();
    private final ProxyConfiguration proxyConfiguration;

    public NettyHttpClient() {
        this(null);
    }

    public NettyHttpClient(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
    }

    public SettableFuture<HttpResponse> sendRequest(final HttpRequest httpRequest) throws SocketConnectionException {
        return sendRequest(httpRequest, httpRequest.socketAddressFromHostHeader());
    }

    public SettableFuture<HttpResponse> sendRequest(final HttpRequest httpRequest, @Nullable InetSocketAddress remoteAddress) throws SocketConnectionException {
        return sendRequest(httpRequest, remoteAddress, ConfigurationProperties.socketConnectionTimeout());
    }

    public SettableFuture<HttpResponse> sendRequest(final HttpRequest httpRequest, @Nullable InetSocketAddress remoteAddress, Integer connectionTimeoutMillis) throws SocketConnectionException {
        if (proxyConfiguration != null && proxyConfiguration.getType() == Type.HTTP) {
            remoteAddress = proxyConfiguration.getProxyAddress();
        } else if (remoteAddress == null) {
            remoteAddress = httpRequest.socketAddressFromHostHeader();
        }

        final SettableFuture<HttpResponse> httpResponseSettableFuture = SettableFuture.create();
        new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024, 32 * 1024))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
            .attr(SECURE, httpRequest.isSecure() != null && httpRequest.isSecure())
            .attr(REMOTE_SOCKET, remoteAddress)
            .attr(REQUEST, httpRequest)
            .attr(RESPONSE_FUTURE, httpResponseSettableFuture)
            .handler(new HttpClientInitializer(proxyConfiguration, mockServerLogger))
            .connect(remoteAddress)
            .addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // send the HTTP request
                        future.channel().writeAndFlush(httpRequest);
                    } else {
                        httpResponseSettableFuture.setException(future.cause());
                    }
                }
            });

        return httpResponseSettableFuture;
    }

    public HttpResponse sendRequest(HttpRequest httpRequest, long timeout, TimeUnit unit) {
        try {
            return sendRequest(httpRequest).get(timeout, unit);
        } catch (TimeoutException e) {
            throw new SocketCommunicationException("Response was not received from MockServer after " + ConfigurationProperties.maxSocketTimeout() + " milliseconds, to make the proxy wait longer please use \"mockserver.maxSocketTimeout\" system property or ConfigurationProperties.maxSocketTimeout(long milliseconds)", e.getCause());
        } catch (InterruptedException | ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SocketConnectionException) {
                throw (SocketConnectionException) cause;
            } else if (cause instanceof ConnectException) {
                throw new SocketConnectionException("Unable to connect to socket " + httpRequest.socketAddressFromHostHeader(), cause);
            } else if (cause instanceof UnknownHostException) {
                throw new SocketConnectionException("Unable to resolve host " + httpRequest.socketAddressFromHostHeader(), cause);
            } else if (cause instanceof IOException) {
                throw new SocketConnectionException(cause.getMessage(), cause);
            } else {
                throw new RuntimeException("Exception while sending request - " + ex.getMessage(), ex);
            }
        }
    }
}
