package com.itorix.apiwiz.common.model.projectmanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyConnection {

	private String orgName;
	private String envName;
	private String isSaaS;
	private String proxyEndpoint;
	private String proxyURL;

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getIsSaaS() {
		return isSaaS;
	}

	public void setIsSaaS(String isSaaS) {
		this.isSaaS = isSaaS;
	}

	public String getProxyEndpoint() {
		return proxyEndpoint;
	}

	public void setProxyEndpoint(String proxyEndpoint) {
		this.proxyEndpoint = proxyEndpoint;
	}

	public String getProxyURL() {
		return proxyURL;
	}

	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}

	@Override
	public String toString() {
		return "ProxyConnection [orgName=" + orgName + ", envName=" + envName + ", isSaaS=" + isSaaS
				+ ", proxyEndpoint=" + proxyEndpoint + ", proxyURL=" + proxyURL + "]";
	}
}
