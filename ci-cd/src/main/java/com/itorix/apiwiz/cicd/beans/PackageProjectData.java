package com.itorix.apiwiz.cicd.beans;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageProjectData {
	
	private String projectName;
	private List<PackagePipelineData> pipelines;
	
	private String proxyName;
	
	
	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}
	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	/**
	 * @return the pipelines
	 */
	public List<PackagePipelineData> getPipelines() {
		return pipelines;
	}
	/**
	 * @param pipelines the pipelines to set
	 */
	public void setPipelines(List<PackagePipelineData> pipelines) {
		this.pipelines = pipelines;
	}
	public String getProxyName() {
		return proxyName;
	}
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}
	
}
