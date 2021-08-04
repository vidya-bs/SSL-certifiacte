package com.itorix.apiwiz.identitymanagement.model.errorlog;

public class ErrorLog {
	private String workspace;
	private String interactionId;
	private String timestamp;
	private String url;
	private String applicationName;

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getInteractionId() {
		return interactionId;
	}

	public void setInteractionId(String interactionId) {
		this.interactionId = interactionId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public String toString() {
		return "ErrorLog [workspace=" + workspace + ", interactionId=" + interactionId + ", timestamp=" + timestamp
				+ ", url=" + url + ", applicationName=" + applicationName + "]";
	}
}
