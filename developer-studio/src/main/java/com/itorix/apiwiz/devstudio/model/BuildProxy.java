package com.itorix.apiwiz.devstudio.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Build.Proxy")
public class BuildProxy extends AbstractObject {
	private String ProxyData;
	private String proxyName;
	public String getProxyData() {
		return ProxyData;
	}
	public void setProxyData(String proxyData) {
		ProxyData = proxyData;
	}
	public String getProxyName() {
		return proxyName;
	}
	public void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}
	
	
	
}
