package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProxyArtifacts {
	private List<String> kvms;// = Arrays.asList("ABF_KVM", "ErrorCodes_KVM");
	private List<String> caches;// = Arrays.asList("ABF_cache", "Stores_cache");
	private List<String> targetServers;// = Arrays.asList("PaymentWSIL_Target", "PaymentDetails_Target");
	private List<String> sharedflows;
	private List<ProxyEndpoint> proxyEndpoints;
	
	public List<String> getKvms() {
		return kvms;
	}
	public void setKvms(List<String> kvms) {
		this.kvms = kvms;
	}
	public List<String> getCaches() {
		return caches;
	}
	public void setCaches(List<String> caches) {
		this.caches = caches;
	}
	public List<String> getTargetServers() {
		return targetServers;
	}
	public void setTargetServers(List<String> targetServers) {
		this.targetServers = targetServers;
	}
	public List<ProxyEndpoint> getProxyEndpoints() {
		return proxyEndpoints;
	}
	public void setProxyEndpoint(List<ProxyEndpoint> proxyEndpoints) {
		this.proxyEndpoints = proxyEndpoints;
	}
	public List<String> getSharedflows() {
		return sharedflows;
	}
	public void setSharedflows(List<String> sharedflows) {
		this.sharedflows = sharedflows;
	}
}
