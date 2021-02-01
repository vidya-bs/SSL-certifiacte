package com.itorix.hyggee.mockserver.mock.action;

import com.itorix.hyggee.mockserver.callback.WebSocketClientRegistry;
import com.itorix.hyggee.mockserver.callback.WebSocketResponseCallback;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.HttpStateHandler;
import com.itorix.hyggee.mockserver.model.HttpObjectCallback;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.responsewriter.ResponseWriter;

import java.util.UUID;

import static com.itorix.hyggee.mockserver.callback.WebSocketClientRegistry.WEB_SOCKET_CORRELATION_ID_HEADER_NAME;
import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.EXPECTATION_RESPONSE;

/**
 *   
 */
public class HttpResponseObjectCallbackActionHandler {
    private final MockServerLogger logFormatter;
    private WebSocketClientRegistry webSocketClientRegistry;

    public HttpResponseObjectCallbackActionHandler(HttpStateHandler httpStateHandler) {
        this.webSocketClientRegistry = httpStateHandler.getWebSocketClientRegistry();
        this.logFormatter = httpStateHandler.getMockServerLogger();
    }

    public void handle(final HttpObjectCallback httpObjectCallback, final HttpRequest request, final ResponseWriter responseWriter) {
        String clientId = httpObjectCallback.getClientId();
        String webSocketCorrelationId = UUID.randomUUID().toString();
        webSocketClientRegistry.registerCallbackHandler(webSocketCorrelationId, new WebSocketResponseCallback() {
            @Override
            public void handle(HttpResponse response) {
                responseWriter.writeResponse(request, response.removeHeader(WEB_SOCKET_CORRELATION_ID_HEADER_NAME), false);
                logFormatter.info(EXPECTATION_RESPONSE, request, "returning response:{}for request:{}for action:{}", response, request, httpObjectCallback);
            }
        });
        webSocketClientRegistry.sendClientMessage(clientId, request.clone().withHeader(WEB_SOCKET_CORRELATION_ID_HEADER_NAME, webSocketCorrelationId));
    }

}
