package com.itorix.apiwiz.common.model.apigee;

import java.util.List;

public class VirtualHost {

	private String name;
	private String port;
	private List<String> hostAliases;
	private List<String> interfaces;
	private SSLInfo sSLInfo;
	private List<String> listenOptions;
	private List<String> retryOptions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public List<String> getHostAliases() {
		return hostAliases;
	}

	public void setHostAliases(List<String> hostAliases) {
		this.hostAliases = hostAliases;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public SSLInfo getsSLInfo() {
		return sSLInfo;
	}

	public void setsSLInfo(SSLInfo sSLInfo) {
		this.sSLInfo = sSLInfo;
	}

	public List<String> getListenOptions() {
		return listenOptions;
	}

	public void setListenOptions(List<String> listenOptions) {
		this.listenOptions = listenOptions;
	}

	public List<String> getRetryOptions() {
		return retryOptions;
	}

	public void setRetryOptions(List<String> retryOptions) {
		this.retryOptions = retryOptions;
	}

	@Override
	public String toString() {
		return "VirtualHost [name=" + name + ", port=" + port + ", hostAliases=" + hostAliases + ", interfaces="
				+ interfaces + ", sSLInfo=" + sSLInfo + ", listenOptions=" + listenOptions + ", retryOptions="
				+ retryOptions + "]";
	}
}
