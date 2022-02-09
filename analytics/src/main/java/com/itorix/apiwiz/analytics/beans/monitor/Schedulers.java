package com.itorix.apiwiz.analytics.beans.monitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedulers {
	String id;
	private int interval;

	@JsonProperty("isPause")
	private boolean pause;

	private String environmentId;
	private String environmentName;
	private long timeout;
	private long lastExecutionTime;
}
