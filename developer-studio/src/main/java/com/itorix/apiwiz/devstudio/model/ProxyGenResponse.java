package com.itorix.apiwiz.devstudio.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.proxystudio.ProxySCMDetails;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyGenResponse {

	private String proxyName;
	private String version;
	private String downloadURI;
	private List<Artifact> kvms;
	private List<Artifact> caches;
	private List<Artifact> targetServers;
	private ProxySCMDetails proxySCMDetails;

	public String getDownloadURI() {
		return downloadURI;
	}

	public void setDownloadURI(String downloadURI) {
		this.downloadURI = downloadURI;
	}

	public List<Artifact> getKvms() {
		return kvms;
	}

	public void setKvms(List<Artifact> kvms) {
		this.kvms = kvms;
	}

	public List<Artifact> getCaches() {
		return caches;
	}

	public void setCaches(List<Artifact> caches) {
		this.caches = caches;
	}

	public List<Artifact> getTargetServers() {
		return targetServers;
	}

	public void setTargetServers(List<Artifact> targetServers) {
		this.targetServers = targetServers;
	}

	public ProxySCMDetails getProxySCMDetails() {
		return proxySCMDetails;
	}

	public void setProxySCMDetails(ProxySCMDetails proxySCMDetails) {
		this.proxySCMDetails = proxySCMDetails;
	}

	public String getProxyName() {
		return proxyName;
	}

	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
