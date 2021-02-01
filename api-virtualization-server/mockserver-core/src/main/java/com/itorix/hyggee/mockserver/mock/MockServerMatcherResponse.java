package com.itorix.hyggee.mockserver.mock;

import java.util.Map;

public class MockServerMatcherResponse {
	private Expectation expectation;
	@SuppressWarnings("rawtypes")
	private Map partialMatches;
	public Expectation getExpectation() {
		return expectation;
	}
	public void setExpectation(Expectation expectation) {
		this.expectation = expectation;
	}
	public Map getPartialMatches() {
		return partialMatches;
	}
	public void setPartialMatches(Map partialMatches) {
		this.partialMatches = partialMatches;
	}
	
}
