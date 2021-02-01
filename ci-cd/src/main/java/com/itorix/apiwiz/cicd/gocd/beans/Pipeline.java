package com.itorix.apiwiz.cicd.gocd.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label_template",
    "enable_pipeline_locking",
    "name",
    "template",
    "origin",
    "parameters",
    "environment_variables",
    "materials",
    "stages",
    "tracking_tool",
    "timer"
})
public class Pipeline {

    @JsonProperty("label_template")
    private String labelTemplate;
    @JsonProperty("enable_pipeline_locking")
    private Boolean enablePipelineLocking;
    @JsonProperty("name")
    private String name;
    @JsonProperty("template")
    private Object template;
    @JsonProperty("origin")
    private Origin origin;
    @JsonProperty("parameters")
    private List<Object> parameters = null;
    @JsonProperty("environment_variables")
    private List<EnvironmentVariable> environmentVariables = null;
    @JsonProperty("materials")
    private List<Material> materials = null;
    @JsonProperty("stages")
    private List<Stage> stages = null;
    @JsonProperty("tracking_tool")
    private Object trackingTool;
    @JsonProperty("timer")
    private Object timer;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
    public Pipeline() {}
    
    public Pipeline(String name, String labelTemplate, boolean enablePipelineLocking) {
    		this.name = name;
    		this.labelTemplate = labelTemplate;
    		this.enablePipelineLocking = enablePipelineLocking;
    }

    @JsonProperty("label_template")
    public String getLabelTemplate() {
        return labelTemplate;
    }

    @JsonProperty("label_template")
    public void setLabelTemplate(String labelTemplate) {
        this.labelTemplate = labelTemplate;
    }

    @JsonProperty("enable_pipeline_locking")
    public Boolean getEnablePipelineLocking() {
        return enablePipelineLocking;
    }

    @JsonProperty("enable_pipeline_locking")
    public void setEnablePipelineLocking(Boolean enablePipelineLocking) {
        this.enablePipelineLocking = enablePipelineLocking;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("template")
    public Object getTemplate() {
        return template;
    }

    @JsonProperty("template")
    public void setTemplate(Object template) {
        this.template = template;
    }

    @JsonProperty("origin")
    public Origin getOrigin() {
        return origin;
    }

    @JsonProperty("origin")
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    @JsonProperty("parameters")
    public List<Object> getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    @JsonProperty("environment_variables")
    public List<EnvironmentVariable> getEnvironmentVariables() {
        return environmentVariables;
    }

    @JsonProperty("environment_variables")
    public void setEnvironmentVariables(List<EnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @JsonProperty("materials")
    public List<Material> getMaterials() {
        return materials;
    }

    @JsonProperty("materials")
    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    @JsonProperty("stages")
    public List<Stage> getStages() {
        return stages;
    }

    @JsonProperty("stages")
    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    @JsonProperty("tracking_tool")
    public Object getTrackingTool() {
        return trackingTool;
    }

    @JsonProperty("tracking_tool")
    public void setTrackingTool(Object trackingTool) {
        this.trackingTool = trackingTool;
    }

    @JsonProperty("timer")
    public Object getTimer() {
        return timer;
    }

    @JsonProperty("timer")
    public void setTimer(Object timer) {
        this.timer = timer;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
