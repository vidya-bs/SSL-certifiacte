package com.itorix.apiwiz.testsuite.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TestSuites.Metadata")
public class MetaData {

	private String metadata;

	public MetaData() {
	}

	public MetaData(String metadata) {
		this.metadata = metadata;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}
