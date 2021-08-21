package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIProxyResponse {
	private String[] revision;

	private String name;

	private MetaData metaData;

	public String[] getRevision() {
		return revision;
	}

	public void setRevision(String[] revision) {
		this.revision = revision;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	@Override
	public String toString() {
		return "ClassPojo [revision = " + revision + ", name = " + name + ", metaData = " + metaData + "]";
	}
}
