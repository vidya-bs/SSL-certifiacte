package com.itorix.apiwiz.design.studio.model.swagger.sync;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Model {

	private String name;
	private Status status;
	private String id;
	private String modelId;
	private Integer revision;

	public enum Status {
		Active, Deprecated,Draft
	}

	private long importTimeStamp;
}