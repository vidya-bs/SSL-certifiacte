package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DictionarySwagger {
	private String name;
	private String id;
	private Integer revision;
	private Status status;
	public enum Status{
		Publish, Deprecated,Draft
	}
	@JsonProperty("schemas")
	private List<SchemaInfo> schemas;
}