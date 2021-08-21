package com.itorix.apiwiz.common.model.policyperformance;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutedFlowAndPolicies {
	Map<String, List<String>> executedPoliciesMap;
	Map<String, List<String>> executedFlowMap;

	public Map<String, List<String>> getExecutedPoliciesMap() {
		return executedPoliciesMap;
	}

	public void setExecutedPoliciesMap(Map<String, List<String>> executedPoliciesMap) {
		this.executedPoliciesMap = executedPoliciesMap;
	}

	public Map<String, List<String>> getExecutedFlowMap() {
		return executedFlowMap;
	}

	public void setExecutedFlowMap(Map<String, List<String>> executedFlowMap) {
		this.executedFlowMap = executedFlowMap;
	}
}
