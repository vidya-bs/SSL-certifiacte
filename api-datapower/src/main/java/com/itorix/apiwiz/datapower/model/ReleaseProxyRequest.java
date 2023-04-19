package com.itorix.apiwiz.datapower.model;

import java.io.Serializable;

public class ReleaseProxyRequest implements Serializable {
	private String releaseTag;

	public String getReleaseTag() {
		return releaseTag;
	}

	public void setReleaseTag(String releaseTag) {
		this.releaseTag = releaseTag;
	}

}
