package com.itorix.apiwiz.cicd.beans;

import java.util.List;

public class PackagePipelineData {
	
	private String name;
	private PackageProxy proxy;	
	private List<String> stages;
	private List<String> buildNumbers;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the stages
	 */
	public List<String> getStages() {
		return stages;
	}
	/**
	 * @param stages the stages to set
	 */
	public void setStages(List<String> stages) {
		this.stages = stages;
	}
	/**
	 * @return the buildNumbers
	 */
	public List<String> getBuildNumbers() {
		return buildNumbers;
	}
	/**
	 * @param buildNumbers the buildNumbers to set
	 */
	public void setBuildNumbers(List<String> buildNumbers) {
		this.buildNumbers = buildNumbers;
	}
	/**
	 * @return the proxy
	 */
	public PackageProxy getProxy() {
		return proxy;
	}
	/**
	 * @param proxy the proxy to set
	 */
	public void setProxy(PackageProxy proxy) {
		this.proxy = proxy;
	}

}
