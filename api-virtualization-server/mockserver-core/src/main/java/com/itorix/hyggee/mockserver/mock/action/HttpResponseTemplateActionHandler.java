package com.itorix.hyggee.mockserver.mock.action;

import static com.itorix.hyggee.mockserver.model.HttpResponse.notFoundResponse;

import com.itorix.hyggee.mockserver.client.serialization.model.HttpResponseDTO;
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
public class HttpResponseTemplateActionHandler {

    private JavaScriptTemplateEngine javaScriptTemplateEngine;
    private VelocityTemplateEngine velocityTemplateEngine;

    public HttpResponseTemplateActionHandler(MockServerLogger logFormatter) {
        javaScriptTemplateEngine = new JavaScriptTemplateEngine(logFormatter);
        velocityTemplateEngine = new VelocityTemplateEngine(logFormatter);
    }

    public HttpResponse handle(Expectation expectation, HttpTemplate httpTemplate, HttpRequest httpRequest) {
        HttpResponse httpResponse = notFoundResponse();
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
            HttpResponse templatedResponse = templateEngine.executeTemplate(expectation, httpTemplate, httpRequest, HttpResponseDTO.class);
            if (templatedResponse != null) {
                return templatedResponse;
            }
        }

        return httpResponse;
    }

}
