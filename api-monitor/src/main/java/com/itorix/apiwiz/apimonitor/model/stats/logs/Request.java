package com.itorix.apiwiz.apimonitor.model.stats.logs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.apimonitor.model.request.Body;
import com.itorix.apiwiz.apimonitor.model.request.FormParam;
import com.itorix.apiwiz.apimonitor.model.request.Header;
import com.itorix.apiwiz.apimonitor.model.request.QueryParam;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
	public String uri;
	public String method;

	@JsonProperty("headers")
	private List<Header> headers = null;

	@JsonProperty("queryParams")
	private List<QueryParam> queryParams = null;

	@JsonProperty("formData")
	private List<FormParam> formParams = null;

	@JsonProperty("formURLEncoded")
	private List<FormParam> formURLEncoded = null;

	@JsonProperty("payload")
	private Body body;
}
