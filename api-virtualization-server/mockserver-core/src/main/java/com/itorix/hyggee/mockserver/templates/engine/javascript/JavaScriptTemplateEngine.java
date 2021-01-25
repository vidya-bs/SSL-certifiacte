package com.itorix.hyggee.mockserver.templates.engine.javascript;

import com.itorix.hyggee.mockserver.client.serialization.model.DTO;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpTemplate;
import com.itorix.hyggee.mockserver.templates.engine.TemplateEngine;
import com.itorix.hyggee.mockserver.templates.engine.model.HttpRequestTemplateObject;
import com.itorix.hyggee.mockserver.templates.engine.serializer.HttpTemplateOutputDeserializer;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.formatting.StringFormatter.formatLogMessage;
import static com.itorix.hyggee.mockserver.formatting.StringFormatter.indentAndToString;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.TEMPLATE_GENERATED;

/**
 *   
 */
public class JavaScriptTemplateEngine implements TemplateEngine {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    private final MockServerLogger logFormatter;
    private HttpTemplateOutputDeserializer httpTemplateOutputDeserializer;

    public JavaScriptTemplateEngine(MockServerLogger logFormatter) {
        this.logFormatter = logFormatter;
        this.httpTemplateOutputDeserializer = new HttpTemplateOutputDeserializer(logFormatter);
    }

    @Override
    public <T> T executeTemplate(Expectation expectation, HttpTemplate httpTemplate, HttpRequest request, Class<? extends DTO<T>> dtoClass) {
        T result = null;
        String script = "function handle(request) {" + indentAndToString(httpTemplate.getTemplate())[0] + "}";
        try {
            if (engine != null) {
                engine.eval(script + " function serialise(request) { return JSON.stringify(handle(JSON.parse(request)), null, 2); }");
                // HttpResponse handle(HttpRequest httpRequest) - ES5
                Object stringifiedResponse = ((Invocable) engine).invokeFunction("serialise", new HttpRequestTemplateObject(request));
                logFormatter.info(TEMPLATE_GENERATED, request, "generated output:{}from template:{}for request:{}", stringifiedResponse, script, request);
                result = httpTemplateOutputDeserializer.deserializer(request, (String) stringifiedResponse, dtoClass);
            } else {
                logFormatter.error(request, "JavaScript based templating is only available in a JVM with the \"nashorn\" JavaScript engine, " +
                    "please use a JVM with the \"nashorn\" JavaScript engine, such as Oracle Java 8+", new RuntimeException("\"nashorn\" JavaScript engine not available"));
            }
        } catch (Exception e) {
            throw new RuntimeException(formatLogMessage("Exception transforming template:{}for request:{}", script, request), e);
        }
        return result;
    }
}
