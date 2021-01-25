package com.itorix.apiwiz.performance.coverge.model;

public class PolicyStatus {
private String policyName;
private boolean isExecuted;
public String getPolicyName() {
	return policyName;
}
public void setPolicyName(String policyName) {
	this.policyName = policyName;
}
public boolean isExecuted() {
	return isExecuted;
}
public void setExecuted(boolean isExecuted) {
	this.isExecuted = isExecuted;
}


public String toString(){
	return policyName+" "+isExecuted;
}
}
