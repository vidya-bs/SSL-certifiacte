package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SchemaInfo {
	private String name;
	private String modelId;
	private Integer revision;
	private Status status;
	public enum Status{
		Active , Deprecated,Draft
	}
	@JsonProperty("swaggers")
	private List<SwaggerData> swaggers;

}