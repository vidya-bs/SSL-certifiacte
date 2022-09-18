package com.itorix.apiwiz.common.model.apigee;

import java.util.List;

public class Mappings {

	private List<Proxy> proxies;
	private List<Sharedflow> sharedflows;
	public List<Proxy> getProxies() {
		return proxies;
	}
	public void setProxies(List<Proxy> proxies) {
		this.proxies = proxies;
	}
	public List<Sharedflow> getSharedflows() {
		return sharedflows;
	}
	public void setSharedflows(List<Sharedflow> sharedflows) {
		this.sharedflows = sharedflows;
	}

}
