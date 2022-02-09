package com.itorix.apiwiz.analytics.beans.monitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

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
