package com.itorix.apiwiz.common.model.proxystudio;

import java.util.Arrays;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Metadata {
	private List<String> kvm = Arrays.asList("ABF_KVM", "ErrorCodes_KVM");
	private List<String> caches = Arrays.asList("ABF_cache", "Stores_cache");
	private List<String> targetServers = Arrays.asList("PaymentWSIL_Target", "PaymentDetails_Target");
	public List<String> getKvm() {
		return kvm;
	}
	public void setKvm(List<String> kvm) {
		this.kvm = kvm;
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
}
