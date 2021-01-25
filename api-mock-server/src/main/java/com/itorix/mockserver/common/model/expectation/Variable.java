package com.itorix.mockserver.common.model.expectation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Variable {
	public enum Ref {
		header, path, queryParams, body
	}

	Ref ref;
	String name;
	String path;
}
