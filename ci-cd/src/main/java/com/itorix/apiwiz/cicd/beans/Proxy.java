package com.itorix.apiwiz.cicd.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxy {

	private String pipelineName;
	private String buildNumber;
	private String stage;
	private PackageProxy proxy;

	/** @return the pipelineName */
	public String getPipelineName() {
		return pipelineName;
	}

	/**
	 * @param pipelineName
	 *            the pipelineName to set
	 */
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	/** @return the buildNumber */
	public String getBuildNumber() {
		return buildNumber;
	}

	/**
	 * @param buildNumber
	 *            the buildNumber to set
	 */
	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	/** @return the stage */
	public String getStage() {
		return stage;
	}

	/**
	 * @param stage
	 *            the stage to set
	 */
	public void setStage(String stage) {
		this.stage = stage;
	}

	/** @return the proxy */
	public PackageProxy getProxy() {
		return proxy;
	}

	/**
	 * @param proxy
	 *            the proxy to set
	 */
	public void setProxy(PackageProxy proxy) {
		this.proxy = proxy;
	}
}
