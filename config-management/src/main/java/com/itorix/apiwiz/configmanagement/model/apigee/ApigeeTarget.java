package com.itorix.apiwiz.configmanagement.model.apigee;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApigeeTarget {
	private Integer port;

	@JsonProperty("sSLInfo")
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

	public SSLInfo getSSLInfo() {
		return sSLInfo;
	}

	public void setSSLInfo(SSLInfo sSLInfo) {
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

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public String toString() {
		return "ApigeeTarget [port=" + port + ", sSLInfo=" + sSLInfo + ", host=" + host + ", name=" + name
				+ ", isEnabled=" + isEnabled + "]";
	}
	
}
