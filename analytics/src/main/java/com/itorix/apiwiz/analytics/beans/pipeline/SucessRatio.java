package com.itorix.apiwiz.analytics.beans.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SucessRatio {

	@JsonProperty("name")
	private String name;

	@JsonProperty("sucessRatio")
	private int sucessRatio;

	@JsonProperty("total")
	private int total;

	@JsonProperty("success")
	private int success;
}
