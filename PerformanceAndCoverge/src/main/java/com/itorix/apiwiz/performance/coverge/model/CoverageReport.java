package com.itorix.apiwiz.performance.coverge.model;

public class CoverageReport {
	private FlowExecutions preFlow;
	private FlowExecutions requestFlow;
	private FlowExecutions responseFlow;
	private FlowExecutions postFlow;

	public FlowExecutions getPreFlow() {
		return preFlow;
	}

	public void setPreFlow(FlowExecutions preFlow) {
		this.preFlow = preFlow;
	}

	public FlowExecutions getRequestFlow() {
		return requestFlow;
	}

	public void setRequestFlow(FlowExecutions requestFlow) {
		this.requestFlow = requestFlow;
	}

	public FlowExecutions getResponseFlow() {
		return responseFlow;
	}

	public void setResponseFlow(FlowExecutions responseFlow) {
		this.responseFlow = responseFlow;
	}

	public FlowExecutions getPostFlow() {
		return postFlow;
	}

	public void setPostFlow(FlowExecutions postFlow) {
		this.postFlow = postFlow;
	}

	public String toString() {
		String pre = "";
		String req = "";
		String res = "";
		String pos = "";

		if (preFlow != null)
			pre = "preflow:" + preFlow.getFlowType() + ":" + preFlow + "\n";
		if (requestFlow != null)
			req = "requestFlow:" + requestFlow.getFlowType() + ":" + requestFlow + "\n";
		if (responseFlow != null)
			res = "responseFlow:" + responseFlow.getFlowType() + ":" + responseFlow + "\n";
		if (postFlow != null)
			pos = "postFlow:" + postFlow.getFlowType() + ":" + postFlow;
		return pre + req + res + pos;
	}
}
