package com.itorix.apiwiz.apimonitor.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MonitorRequest {

	enum Schemes {
		http, https;
	}

	private String id;
	private String name;
	private String summary;
	@JsonProperty("isPause")
	private boolean pause;
	private String host;
	private String port;
	private String schemes;
	private String path;
	private String verb;
	private String sslReference;
	private Request request;
	private Response response;
	private long expectedLatency;
}
