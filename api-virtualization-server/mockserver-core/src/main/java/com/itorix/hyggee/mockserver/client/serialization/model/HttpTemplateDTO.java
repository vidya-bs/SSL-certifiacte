package com.itorix.hyggee.mockserver.client.serialization.model;

import java.util.List;

import com.itorix.hyggee.mockserver.model.HttpTemplate;
import com.itorix.hyggee.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;
import com.itorix.hyggee.mockserver.model.Variable;
import com.itorix.hyggee.mockserver.model.Variables;


public class HttpTemplateDTO extends ObjectWithReflectiveEqualsHashCodeToString implements DTO<HttpTemplate> {

    private String template;
    private HttpTemplate.TemplateType templateType;
    private DelayDTO delay;
    private List<Variable> variables;

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

    public HttpTemplateDTO(HttpTemplate httpTemplate) {
        if (httpTemplate != null) {
            templateType = httpTemplate.getTemplateType();
            template = httpTemplate.getTemplate();
            delay = (httpTemplate.getDelay() != null ? new DelayDTO(httpTemplate.getDelay()) : null);
            variables = httpTemplate.getVariables();
        }
    }

    public HttpTemplateDTO() {
    }

    public HttpTemplate buildObject() {
        return new HttpTemplate(templateType)
            .withTemplate(template)
            .withDelay((delay != null ? delay.buildObject() : null))
            .withVariables(variables);
        
    }

    public HttpTemplate.TemplateType getTemplateType() {
        return templateType;
    }

    public HttpTemplateDTO setTemplateType(HttpTemplate.TemplateType templateType) {
        this.templateType = templateType;
        return this;
    }

    public String getTemplate() {
        return template;
    }

    public HttpTemplateDTO setTemplate(String template) {
        this.template = template;
        return this;
    }

    public DelayDTO getDelay() {
        return delay;
    }

    public HttpTemplateDTO setDelay(DelayDTO delay) {
        this.delay = delay;
        return this;
    }

//	public Variables getVariables() {
//		return variables;
//	}
//
//	public Variables setVariables(Variables variables) {
//		this.variables = variables;
//		return variables;
//	}
}

