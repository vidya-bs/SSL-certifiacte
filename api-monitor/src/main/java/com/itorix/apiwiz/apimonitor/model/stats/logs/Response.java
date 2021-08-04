package com.itorix.apiwiz.apimonitor.model.stats.logs;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.apimonitor.model.request.Body;
import com.itorix.apiwiz.apimonitor.model.request.Variable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
	public int statusCode;
	public String statusMessage;

	@JsonProperty("headers")
	private Map<String, String> headers;

	@JsonProperty("body")
	private Body body;

	@JsonProperty("variables")
	private List<Variable> variables;
}
