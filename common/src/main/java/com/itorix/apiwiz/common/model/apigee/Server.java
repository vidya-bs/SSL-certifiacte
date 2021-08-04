package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Server {
	private String status;

	private String uUID;

	private String[] type;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public String[] getType() {
		return type;
	}

	public void setType(String[] type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ClassPojo [status = " + status + ", uUID = " + uUID + ", type = " + type + "]";
	}
}
