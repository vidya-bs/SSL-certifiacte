package com.itorix.apiwiz.identitymanagement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "DB.Properties")
public class DBConfig{
	
	@Id
	private String propertyKey;
	private String propertyValue;
	
	public String getPropertyValue() {
		return propertyValue;
	}
	public String getPropertyKey() {
		return propertyKey;
	}
	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public DBConfig() {
	}

	public DBConfig(String propertyKey, String propertyValue) {
		this.propertyKey = propertyKey;
		this.propertyValue = propertyValue;
	}



}