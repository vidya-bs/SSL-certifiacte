package com.itorix.apiwiz.common.model.proxystudio;

import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;

public class PromoteProxyRequest {
	private Scm scm;
	private ProxyPortfolio portfolio;
	public Scm getScm() {
		return scm;
	}
	public void setScm(Scm scm) {
		this.scm = scm;
	}
	public ProxyPortfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(ProxyPortfolio portfolio) {
		this.portfolio = portfolio;
	}

}
