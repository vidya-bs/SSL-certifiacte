package com.itorix.apiwiz.datapower.model;

import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import java.io.Serializable;

public class PromoteProxyRequest implements Serializable {
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
