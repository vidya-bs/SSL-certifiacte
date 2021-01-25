package com.itorix.hyggee.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.callback.WebSocketClientRegistry;
import com.itorix.hyggee.mockserver.callback.WebSocketRequestCallback;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.log.model.MessageLogEntry;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.HttpStateHandler;
import com.itorix.hyggee.mockserver.model.HttpObjectCallback;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;

import java.util.UUID;

import static com.itorix.hyggee.mockserver.callback.WebSocketClientRegistry.WEB_SOCKET_CORRELATION_ID_HEADER_NAME;
import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.EXPECTATION_RESPONSE;

/**
 *   
 */
public class HttpForwardObjectCallbackActionHandler extends HttpForwardAction {
    private final MockServerLogger logFormatter;
    private final Scheduler scheduler;
    private WebSocketClientRegistry webSocketClientRegistry;

    public HttpForwardObjectCallbackActionHandler(HttpStateHandler httpStateHandler, NettyHttpClient httpClient) {
        super(httpStateHandler.getMockServerLogger(), httpClient);
        this.scheduler = httpStateHandler.getScheduler();
        this.webSocketClientRegistry = httpStateHandler.getWebSocketClientRegistry();
        this.logFormatter = httpStateHandler.getMockServerLogger();
    }

    public void handle(final HttpObjectCallback httpObjectCallback, final HttpRequest request, final ResponseWriter responseWriter, final boolean synchronous) {
        String clientId = httpObjectCallback.getClientId();
        String webSocketCorrelationId = UUID.randomUUID().toString();
        webSocketClientRegistry.registerCallbackHandler(webSocketCorrelationId, new WebSocketRequestCallback() {
            @Override
            public void handle(final HttpRequest request) {
                final SettableFuture<HttpResponse> responseFuture = sendRequest(request.removeHeader(WEB_SOCKET_CORRELATION_ID_HEADER_NAME), null);
                scheduler.submit(responseFuture, new Runnable() {
                    public void run() {
                        try {
                            HttpResponse response = responseFuture.get();
                            responseWriter.writeResponse(request, response, false);
                            logFormatter.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, httpObjectCallback);
                        } catch (Exception ex) {
                            logFormatter.error(request, ex, ex.getMessage());
                        }
                    }
                }, synchronous);
            }
        });
        webSocketClientRegistry.sendClientMessage(clientId, request.clone().withHeader(WEB_SOCKET_CORRELATION_ID_HEADER_NAME, webSocketCorrelationId));
    }

}
