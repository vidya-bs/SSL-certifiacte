package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Level {

	private String levelname;
	private Values[] values;
	public String getLevelname() {
		return levelname;
	}
	public void setLevelname(String levelname) {
		this.levelname = levelname;
	}
	public Values[] getValues() {
		return values;
	}
	public void setValues(Values[] values) {
		this.values = values;
	}
	
	
	
	
}
