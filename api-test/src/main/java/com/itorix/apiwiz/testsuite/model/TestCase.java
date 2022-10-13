package com.itorix.apiwiz.testsuite.model;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TestCase {

	@JsonProperty("id")
	private String id = UUID.randomUUID().toString();

	@JsonProperty("name")
	private String name;

	@JsonProperty("testCaseSequence")
	private List<String> testCaseSequence;

	@JsonProperty("scenarioSequence")
	private List<String> scenarioSequence;

	@JsonProperty("isTemp")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isTemp = false;

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




}
