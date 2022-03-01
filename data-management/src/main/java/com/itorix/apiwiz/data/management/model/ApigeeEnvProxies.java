package com.itorix.apiwiz.data.management.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Apigee.ProxyNames")
public class ApigeeEnvProxies {
	
	@Id
	private String id;
	private String org;
	private String env;
	private String type;
	private List<String> proxies;
	private Long cts;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getProxies() {
		return proxies;
	}
	public void setProxies(List<String> proxies) {
		this.proxies = proxies;
	}
	public Long getCts() {
		return cts;
	}
	public void setCts(Long cts) {
		this.cts = cts;
	}

}
