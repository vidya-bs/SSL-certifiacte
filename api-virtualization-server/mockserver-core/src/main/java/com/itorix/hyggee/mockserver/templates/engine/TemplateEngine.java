package com.itorix.hyggee.mockserver.templates.engine;

import com.itorix.hyggee.mockserver.client.serialization.model.DTO;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.model.HttpTemplate;

/**
 *   
 */
public interface TemplateEngine {

    <T> T executeTemplate(Expectation expectation, HttpTemplate httpTemplate, HttpRequest httpRequest, Class<? extends DTO<T>> dtoClass);

}
