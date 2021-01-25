package com.itorix.apiwiz.common.model.configmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ProductAttributes {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String name;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	
	
	
}
