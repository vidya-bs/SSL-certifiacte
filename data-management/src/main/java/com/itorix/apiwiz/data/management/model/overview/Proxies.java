package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxies {
	private List<Targetserver> targetservers;

	private String revision;

	private List<String> cache;

	private String name;
	
	private String basePath;
	
	private List<Resources> paths;

	private List<Products> products;

	private List<String> kvm;

	private List<Policy> proxyPolicies;

	private List<Policy> targetPolicies;

	public List<Targetserver> getTargetservers() {
		return targetservers;
	}

	public void setTargetservers(List<Targetserver> targetservers) {
		this.targetservers = targetservers;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public List<String> getCache() {
		return cache;
	}

	public void setCache(List<String> cache) {
		this.cache = cache;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Products> getProducts() {
		return products;
	}

	public void setProducts(List<Products> products) {
		this.products = products;
	}

	public List<String> getKvm() {
		return kvm;
	}

	public void setKvm(List<String> kvm) {
		this.kvm = kvm;
	}

	@Override
	public String toString() {
		return "ClassPojo [targetservers = " + targetservers + ", revision = " + revision + ", cache = " + cache
				+ ", name = " + name + ", products = " + products + ", kvm = " + kvm + "]";
	}

	public List<Policy> getTargetPolicies() {
		return targetPolicies;
	}

	public void setTargetPolicies(List<Policy> targetPolicies) {
		this.targetPolicies = targetPolicies;
	}

	public List<Policy> getProxyPolicies() {
		return proxyPolicies;
	}

	public void setProxyPolicies(List<Policy> proxyPolicies) {
		this.proxyPolicies = proxyPolicies;
	}

	public List<Resources> getPaths() {
		return paths;
	}

	public void setPaths(List<Resources> paths) {
		this.paths = paths;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
