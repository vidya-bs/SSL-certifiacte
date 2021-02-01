package com.itorix.hyggee.mockserver.model;

public class ClosestMatcher {
	
	private String expectationId;
	private String expectationName;
	private String groupName;
	private String reason;
	
	public String getExpectationId() {
		return expectationId;
	}
	public void setExpectationId(String expectationId) {
		this.expectationId = expectationId;
	}
	public String getExpectationName() {
		return expectationName;
	}
	public void setExpectationName(String expectationName) {
		this.expectationName = expectationName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	

}
