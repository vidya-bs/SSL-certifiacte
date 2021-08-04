package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class FlowExecutions {
	private String flowType;
	private List<PolicyStatus> policyStatus;

	public String getFlowType() {
		return flowType;
	}

	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}

	public List<PolicyStatus> getPolicyStatus() {
		return policyStatus;
	}

	public void setPolicyStatus(List<PolicyStatus> policyStatus) {
		this.policyStatus = policyStatus;
	}

	public String toString() {
		String temp = flowType + " ";
		if (policyStatus.size() > 0) {
			for (PolicyStatus s : policyStatus) {
				temp += s;
			}
		}
		return temp;
	}
}
