package com.itorix.hyggee.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import com.itorix.hyggee.mockserver.client.netty.NettyHttpClient;
import com.itorix.hyggee.mockserver.client.serialization.model.HttpRequestDTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpResponse;
import com.itorix.hyggee.mockserver.model.HttpTemplate;
import com.itorix.hyggee.mockserver.templates.engine.TemplateEngine;
import com.itorix.hyggee.mockserver.templates.engine.javascript.JavaScriptTemplateEngine;
import com.itorix.hyggee.mockserver.templates.engine.velocity.VelocityTemplateEngine;

/**
 *   
 */
public class HttpForwardTemplateActionHandler extends HttpForwardAction {

    private JavaScriptTemplateEngine javaScriptTemplateEngine;
    private VelocityTemplateEngine velocityTemplateEngine;

    public HttpForwardTemplateActionHandler(MockServerLogger logFormatter, NettyHttpClient httpClient) {
        super(logFormatter, httpClient);
        javaScriptTemplateEngine = new JavaScriptTemplateEngine(logFormatter);
        velocityTemplateEngine = new VelocityTemplateEngine(logFormatter);
    }

    public SettableFuture<HttpResponse> handle(Expectation expectation, HttpTemplate httpTemplate, HttpRequest originalRequest) {
        TemplateEngine templateEngine = null;
        switch (httpTemplate.getTemplateType()) {
            case VELOCITY:
                templateEngine = velocityTemplateEngine;
                break;
            case JAVASCRIPT:
                templateEngine = javaScriptTemplateEngine;
                break;
            default:
                throw new RuntimeException("Unknown no template engine available for " + httpTemplate.getTemplateType());
        }
        if (templateEngine != null) {
            HttpRequest templatedRequest = templateEngine.executeTemplate(expectation, httpTemplate, originalRequest, HttpRequestDTO.class);
            if (templatedRequest != null) {
                return sendRequest(templatedRequest, null);
            }
        }

        return notFoundFuture();
    }
}
