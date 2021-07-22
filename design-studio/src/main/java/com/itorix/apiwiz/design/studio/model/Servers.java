package com.itorix.apiwiz.design.studio.model;

public enum Servers {
	jaxrs_cxf("jaxrs-cxf"), jaxrs("jaxrs"), nodejs_server("nodejs-server"), spring("spring");

	private String value;

	private Servers(String value) {
		this.value = value;
	}

	public String getClient() {
		return value;
	}

	public void setClient(String value) {
		this.value = value;
	}
}
