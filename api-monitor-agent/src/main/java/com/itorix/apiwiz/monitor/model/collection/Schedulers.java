package com.itorix.apiwiz.monitor.model.collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schedulers {
	String id;
	private int interval;
	@JsonProperty("isPause")
	private boolean pause;
	private String environmentId;
	private String environmentName;
	private long timeout;
}
