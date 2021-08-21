package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class Dimensions {
	private String proxyname;

	private List<Metrics> metrics;

	public String getProxyname() {
		return proxyname;
	}

	public void setProxyname(String proxyname) {
		this.proxyname = proxyname;
	}

	public List<Metrics> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metrics> metrics) {
		this.metrics = metrics;
	}
}
