package com.itorix.hyggee.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.model.HttpOverrideForwardedRequest;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;

/**
 *   
 */
public class HttpOverrideForwardedRequestActionHandler extends HttpForwardAction {

    public HttpOverrideForwardedRequestActionHandler(MockServerLogger logFormatter, NettyHttpClient httpClient) {
        super(logFormatter, httpClient);
    }

    public SettableFuture<HttpResponse> handle(final HttpOverrideForwardedRequest httpOverrideForwardedRequest, final HttpRequest request) {
        return sendRequest(request.clone().update(httpOverrideForwardedRequest.getHttpRequest()), null);
    }

}
