package com.itorix.apiwiz.virtualization.model.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Headers {

	private List<String> contentType = null;
	private List<String> connection = null;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public List<String> getContentType() {
		return contentType;
	}

	public void setContentType(List<String> contentType) {
		this.contentType = contentType;
	}

	public List<String> getConnection() {
		return connection;
	}

	public void setConnection(List<String> connection) {
		this.connection = connection;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}