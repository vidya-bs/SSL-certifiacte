package com.itorix.hyggee.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpForward;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

import java.net.InetSocketAddress;

/**
 *   
 */
public class HttpForwardActionHandler extends HttpForwardAction {

    public HttpForwardActionHandler(MockServerLogger logFormatter, NettyHttpClient httpClient) {
        super(logFormatter, httpClient);
    }

    public SettableFuture<HttpResponse> handle(HttpForward httpForward, HttpRequest httpRequest) {
        if (httpForward.getScheme().equals(HttpForward.Scheme.HTTPS)) {
            httpRequest.withSecure(true);
        } else {
            httpRequest.withSecure(false);
        }
        return sendRequest(httpRequest, new InetSocketAddress(httpForward.getHost(), httpForward.getPort()));
    }

}
