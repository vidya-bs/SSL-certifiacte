package com.itorix.hyggee.mockserver.matchers;

public class HttpRequestMatcherResponse {
	
	private boolean matched;
	private boolean pathMatched;
	private String because;
	
	public boolean isMatched() {
		return matched;
	}
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
	public boolean isPathMatched() {
		return pathMatched;
	}
	public void setPathMatched(boolean pathMatched) {
		this.pathMatched = pathMatched;
	}
	public String getBecause() {
		return because;
	}
	public void setBecause(String because) {
		this.because = because;
	}

}
