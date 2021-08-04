package com.itorix.apiwiz.common.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "APIWIZ.Static.Content")
public class MetaData {

	private String key;
	private String metadata;

	public MetaData() {
	}

	public MetaData(String key, String metadata) {
		this.key = key;
		this.metadata = metadata;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
