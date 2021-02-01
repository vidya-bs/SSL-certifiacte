package com.itorix.apiwiz.cicd.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageMetadata {
	
	private String proxy;
	private String pipelineName;
	private String status;
	
	/**
	 * @return the proxy
	 */
	public String getProxy() {
		return proxy;
	}
	/**
	 * @param proxy the proxy to set
	 */
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}
	/**
	 * @return the pipelineName
	 */
	public String getPipelineName() {
		return pipelineName;
	}
	/**
	 * @param pipelineName the pipelineName to set
	 */
	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
