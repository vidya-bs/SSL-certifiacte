package com.itorix.apiwiz.testsuite.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {

	@JsonProperty("id")
	private String id = UUID.randomUUID().toString();

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("host")
	private String host;

	@JsonProperty("port")
	private String port;

	@JsonProperty("schemes")
	private String schemes;

	@JsonProperty("path")
	private String path;

	@JsonProperty("verb")
	private String verb;

	@JsonProperty("request")
	private Request request;

	@JsonProperty("response")
	private Response response;

	@JsonProperty("status")
	private String status;

	@JsonProperty("dependsOn")
	private String dependsOn;

	@JsonProperty("errorMessage")
	private String message;

	@JsonProperty("isMonitor")
	private boolean monitored;

	@JsonProperty("timeout")
	private Long timeout;

	@JsonProperty("duration")
	private Long duration;

	@JsonProperty("sslReference")
	private String sslReference;

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("host")
	public String getHost() {
		return host;
	}

	@JsonProperty("host")
	public void setHost(String host) {
		this.host = host;
	}

	@JsonProperty("port")
	public String getPort() {
		return port;
	}

	@JsonProperty("port")
	public void setPort(String port) {
		this.port = port;
	}

	@JsonProperty("schemes")
	public String getSchemes() {
		return schemes;
	}

	@JsonProperty("schemes")
	public void setSchemes(String schemes) {
		this.schemes = schemes;
	}

	@JsonProperty("path")
	public String getPath() {
		return path;
	}

	@JsonProperty("path")
	public void setPath(String path) {
		this.path = path;
	}

	@JsonProperty("verb")
	public String getVerb() {
		return verb;
	}

	@JsonProperty("verb")
	public void setVerb(String verb) {
		this.verb = verb;
	}

	@JsonProperty("request")
	public Request getRequest() {
		return request;
	}

	@JsonProperty("request")
	public void setRequest(Request request) {
		this.request = request;
	}

	@JsonProperty("response")
	public Response getResponse() {
		return response;
	}

	@JsonProperty("response")
	public void setResponse(Response response) {
		this.response = response;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(TestCase testCase) {
		return this.id.equals(testCase.getId());
	}

	public String getSslReference() {
		return sslReference;
	}

	public void setSslReference(String sslReference) {
		this.sslReference = sslReference;
	}
}