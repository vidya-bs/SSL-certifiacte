package com.itorix.apiwiz.testsuite.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {

	@JsonProperty("queryParams")
	private List<QueryParam> queryParams = null;

	@JsonProperty("headers")
	private List<Header> headers = null;

	@JsonProperty("body")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Body body;

	@JsonProperty("formParams")
	private List<FormParam> formParams = null;

	@JsonProperty("formURLEncoded")
	private List<FormParam> formURLEncoded = null;

	public List<FormParam> getFormURLEncoded() {
		return formURLEncoded;
	}

	public void setFormURLEncoded(List<FormParam> formURLEncoded) {
		this.formURLEncoded = formURLEncoded;
	}

	@JsonProperty("queryParams")
	public List<QueryParam> getQueryParams() {
		return queryParams;
	}

	@JsonProperty("queryParams")
	public void setQueryParams(List<QueryParam> queryParams) {
		this.queryParams = queryParams;
	}

	@JsonProperty("headers")
	public List<Header> getHeaders() {
		return headers;
	}

	public void addHeader(Header header) {
		if (this.headers == null)
			this.headers = new ArrayList<Header>();
		this.addHeader(header);
	}

	@JsonProperty("headers")
	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	@JsonProperty("body")
	public Body getBody() {
		return body;
	}

	@JsonProperty("body")
	public void setBody(Body body) {
		this.body = body;
	}

	public List<FormParam> getFormParams() {
		return formParams;
	}

	public void setFormParams(List<FormParam> formParams) {
		this.formParams = formParams;
	}
}
