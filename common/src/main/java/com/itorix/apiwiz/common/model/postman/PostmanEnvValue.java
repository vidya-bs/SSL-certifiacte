package com.itorix.apiwiz.common.model.postman;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanEnvValue {
	public String key;
	public String value;
	public String type;
	public String name;

	@Override
	public String toString() {
		return "[" + key + ":" + value + "]";
	}
}
