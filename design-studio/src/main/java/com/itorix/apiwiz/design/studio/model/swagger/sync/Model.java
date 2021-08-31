package com.itorix.apiwiz.design.studio.model.swagger.sync;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Model {

	private String name;
	private Status status;

	public enum Status {
		Active, Deprecated
	}

	private long importTimeStamp;
}
