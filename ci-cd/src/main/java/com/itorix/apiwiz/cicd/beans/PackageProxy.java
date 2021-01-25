package com.itorix.apiwiz.cicd.beans;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageProxy {
	
	private String name;
	private Set<String> products;
	private Set<String> devapps;
	private List<String> kvm;
	private List<String> caches;
	private List<String> targetServers;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the products
	 */
	public Set<String> getProducts() {
		return products;
	}
	/**
	 * @param products the products to set
	 */
	public void setProducts(Set<String> products) {
		this.products = products;
	}
	/**
	 * @return the devapps
	 */
	public Set<String> getDevapps() {
		return devapps;
	}
	/**
	 * @param devapps the devapps to set
	 */
	public void setDevapps(Set<String> devapps) {
		this.devapps = devapps;
	}
	/**
	 * @return the kvm
	 */
	public List<String> getKvm() {
		return kvm;
	}
	/**
	 * @param kvm the kvm to set
	 */
	public void setKvm(List<String> kvm) {
		this.kvm = kvm;
	}
	/**
	 * @return the caches
	 */
	public List<String> getCaches() {
		return caches;
	}
	/**
	 * @param caches the caches to set
	 */
	public void setCaches(List<String> caches) {
		this.caches = caches;
	}
	/**
	 * @return the targetServers
	 */
	public List<String> getTargetServers() {
		return targetServers;
	}
	/**
	 * @param targetServers the targetServers to set
	 */
	public void setTargetServers(List<String> targetServers) {
		this.targetServers = targetServers;
	}
	
}
