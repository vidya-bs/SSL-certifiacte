package com.itorix.apiwiz.monitor.model.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Response {

	@JsonProperty("description")
	private String description;

	@JsonProperty("statusMessage")
	private String message;

	@JsonProperty("headers")
	private Map<String, String> headers;

	@JsonProperty("body")
	private Body body;

	@JsonProperty("variables")
	private List<Variable> variables;
}