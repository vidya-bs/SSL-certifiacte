package com.itorix.apiwiz.design.studio.model.swagger.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.design.studio.model.swagger.sync.Model.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Dictionary {
	private String name;
	private String id;
	private Integer revision;
	private Status status;
	public enum Status {
		Publish, Deprecated,Draft
	}

	@JsonProperty("schemas")
	private List<Model> models;
}