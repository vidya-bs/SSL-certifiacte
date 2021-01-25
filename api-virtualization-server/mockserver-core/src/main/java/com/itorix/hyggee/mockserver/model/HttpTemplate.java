package com.itorix.hyggee.mockserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class HttpTemplate extends Action {
    private final TemplateType templateType;
    private String template;
    private Delay delay;
    private Type actionType;
    private List<Variable> variables;

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

    public HttpTemplate(TemplateType type) {
        this.templateType = type;
    }

    /**
     * Static builder to create an template for responding or forwarding requests.
     */
    public static HttpTemplate template(TemplateType type) {
        return new HttpTemplate(type);
    }

    /**
     * Static builder to create an template for responding or forwarding requests.
     *
     * @param template the template for the response or request
     */
    public static HttpTemplate template(TemplateType type, String template) {
        return new HttpTemplate(type).withTemplate(template);
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public HttpTemplate withTemplate(String template) {
        this.template = template;
        return this;
    }

    public String getTemplate() {
        return template;
    }

    /**
     * The delay before responding with this request as a Delay object, for example new Delay(TimeUnit.SECONDS, 3)
     *
     * @param delay a Delay object, for example new Delay(TimeUnit.SECONDS, 3)
     */
    public HttpTemplate withDelay(Delay delay) {
        this.delay = delay;
        return this;
    }

    /**
     * The delay before responding with this request as a Delay object, for example new Delay(TimeUnit.SECONDS, 3)
     *
     * @param timeUnit a the time unit, for example TimeUnit.SECONDS
     * @param value    a the number of time units to delay the response
     */
    public HttpTemplate withDelay(TimeUnit timeUnit, long value) {
        withDelay(new Delay(timeUnit, value));
        return this;
    }
    
    public HttpTemplate withVariables(List<Variable> variables) {
    	this.variables = variables;
        return this;
    }

    public Delay getDelay() {
        return delay;
    }

    public void setActionType(Type actionType) {
        this.actionType = actionType;
    }

    @Override
    @JsonIgnore
    public Type getType() {
        return actionType;
    }

    public HttpTemplate shallowClone() {
        return template(getTemplateType())
            .withTemplate(getTemplate())
            .withDelay(getDelay());
    }

//    public Variables getVariables() {
//		return variables;
//	}
//
//	public void setVariables(Variables variables) {
//		this.variables = variables;
//	}

	public enum TemplateType {
        JAVASCRIPT,
        VELOCITY
    }

	
}

