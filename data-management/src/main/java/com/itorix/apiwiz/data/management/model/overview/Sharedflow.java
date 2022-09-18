package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sharedflow {

	private String name;
	private String revision;
	private List<String> kvm;
	private List<String> cache;
	private List<String> targetservers;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public List<String> getKvm() {
		return kvm;
	}
	public void setKvm(List<String> kvm) {
		this.kvm = kvm;
	}
	public List<String> getCache() {
		return cache;
	}
	public void setCache(List<String> cache) {
		this.cache = cache;
	}
	public List<String> getTargetservers() {
		return targetservers;
	}
	public void setTargetservers(List<String> targetservers) {
		this.targetservers = targetservers;
	}

}
