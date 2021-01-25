package com.itorix.hyggee.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.filters.HopByHopHeaderFilter;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

import javax.annotation.Nullable;

import static com.itorix.hyggee.mockserver.model.HttpResponse.notFoundResponse;

import java.net.InetSocketAddress;

/**
 *   
 */
public abstract class HttpForwardAction {

    protected final MockServerLogger mockServerLogger;
    private final NettyHttpClient httpClient;
    private HopByHopHeaderFilter hopByHopHeaderFilter = new HopByHopHeaderFilter();

    HttpForwardAction(MockServerLogger mockServerLogger, NettyHttpClient httpClient) {
        this.mockServerLogger = mockServerLogger;
        this.httpClient = httpClient;
    }

    protected SettableFuture<HttpResponse> sendRequest(HttpRequest httpRequest, @Nullable InetSocketAddress remoteAddress) {
        try {
            return httpClient.sendRequest(hopByHopHeaderFilter.onRequest(httpRequest), remoteAddress);
        } catch (Exception e) {
            mockServerLogger.error(httpRequest, e, "Exception forwarding request " + httpRequest);
        }
        return notFoundFuture();
    }

    SettableFuture<HttpResponse> notFoundFuture() {
        SettableFuture<HttpResponse> notFoundFuture = SettableFuture.create();
        notFoundFuture.set(notFoundResponse());
        return notFoundFuture;
    }
}
