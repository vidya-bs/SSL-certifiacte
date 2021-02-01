package com.itorix.apiwiz.performance.coverge.model;

import java.util.HashSet;
import java.util.Set;


public class EndpointStatVO {
	EndpointStat endpointStat;
	 Set<String> totalPoliciesMap=new HashSet<>();
	 Set<String> executedPoliciesMap=new HashSet<>();
	public EndpointStat getEndpointStat() {
		return endpointStat;
	}
	public void setEndpointStat(EndpointStat endpointStat) {
		this.endpointStat = endpointStat;
	}
	public Set<String> getTotalPoliciesMap() {
		return totalPoliciesMap;
	}
	public void setTotalPoliciesMap(Set<String> totalPoliciesMap) {
		this.totalPoliciesMap = totalPoliciesMap;
	}
	public Set<String> getExecutedPoliciesMap() {
		return executedPoliciesMap;
	}
	public void setExecutedPoliciesMap(Set<String> executedPoliciesMap) {
		this.executedPoliciesMap = executedPoliciesMap;
	}

}
