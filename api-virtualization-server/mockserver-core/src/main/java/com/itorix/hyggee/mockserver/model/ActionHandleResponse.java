package com.itorix.hyggee.mockserver.model;

import java.util.List;

import com.itorix.hyggee.mockserver.mock.Expectation;

public class ActionHandleResponse {
	
	private Expectation expectation;
	private List<ClosestMatcher> matchers;
	
	public Expectation getExpectation() {
		return expectation;
	}
	public void setExpectation(Expectation expectation) {
		this.expectation = expectation;
	}
	public List<ClosestMatcher> getMatchers() {
		return matchers;
	}
	public void setMatchers(List<ClosestMatcher> matchers) {
		this.matchers = matchers;
	}
}
