package com.itorix.hyggee.mockserver.client.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;

import com.itorix.hyggee.mockserver.client.netty.codec.MockServerClientCodec;
import com.itorix.hyggee.mockserver.client.netty.proxy.ProxyConfiguration;
import com.itorix.hyggee.mockserver.client.netty.proxy.Type;
import com.itorix.hyggee.mockserver.logging.LoggingHandler;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;

import java.net.InetSocketAddress;

import static com.itorix.hyggee.mockserver.client.netty.NettyHttpClient.REMOTE_SOCKET;
import static com.itorix.hyggee.mockserver.client.netty.NettyHttpClient.SECURE;
import static com.itorix.hyggee.mockserver.socket.NettySslContextFactory.nettySslContextFactory;
import static org.slf4j.event.Level.TRACE;

@ChannelHandler.Sharable
public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {

    private final MockServerLogger mockServerLogger;
    private final HttpClientConnectionHandler httpClientConnectionHandler = new HttpClientConnectionHandler();
    private final HttpClientHandler httpClientHandler;
    private final ProxyConfiguration proxyConfiguration;

    public HttpClientInitializer(ProxyConfiguration proxyConfiguration, MockServerLogger mockServerLogger) {
        this.proxyConfiguration = proxyConfiguration;
        this.mockServerLogger = mockServerLogger;
        this.httpClientHandler = new HttpClientHandler(mockServerLogger);
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        if (proxyConfiguration != null) {
            if (proxyConfiguration.getType() == Type.HTTPS) {
                pipeline.addLast(new HttpProxyHandler(proxyConfiguration.getProxyAddress()));
            } else if (proxyConfiguration.getType() == Type.SOCKS5) {
                pipeline.addLast(new Socks5ProxyHandler(proxyConfiguration.getProxyAddress()));
            }
        }
        pipeline.addLast(httpClientConnectionHandler);

        if (channel.attr(SECURE) != null && channel.attr(SECURE).get() != null && channel.attr(SECURE).get()) {
            InetSocketAddress remoteAddress = channel.attr(REMOTE_SOCKET).get();
            pipeline.addLast(nettySslContextFactory().createClientSslContext().newHandler(channel.alloc(), remoteAddress.getHostName(), remoteAddress.getPort()));
        }

        // add logging
        if (mockServerLogger.isEnabled(TRACE)) {
            pipeline.addLast(new LoggingHandler("NettyHttpClient -->"));
        }

        pipeline.addLast(new HttpClientCodec());

        pipeline.addLast(new HttpContentDecompressor());

        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));

        pipeline.addLast(new MockServerClientCodec());

        pipeline.addLast(httpClientHandler);
    }
}
