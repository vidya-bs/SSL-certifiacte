package com.itorix.apiwiz.configmanagement.model.apigeeX;

import com.itorix.apiwiz.configmanagement.model.apigee.SSLInfo;

public class ApigeexTarget {

	private Integer port;

	private SSLInfo sSLInfo;

	private String host;

	private String name;

	private boolean isEnabled;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public SSLInfo getsSLInfo() {
		return sSLInfo;
	}

	public void setsSLInfo(SSLInfo sSLInfo) {
		this.sSLInfo = sSLInfo;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
}
