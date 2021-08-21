package com.itorix.apiwiz.common.model.apigee;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.Apigee.Configuration")
public class ApigeeConfigurationVO extends AbstractObject {
	private String port;

	private String orgname;

	private String hostname;

	private String type;

	private String scheme;

	private List<String> environments;

	private ApigeeServiceUser apigeeServiceUser;

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getOrgname() {
		return orgname;
	}

	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {
		this.environments = environments;
	}

	@JsonIgnore
	public ApigeeServiceUser getApigeeServiceUser() {
		return apigeeServiceUser;
	}

	@JsonIgnore
	public void setApigeeServiceUser(ApigeeServiceUser apigeeServiceUser) {
		this.apigeeServiceUser = apigeeServiceUser;
	}
}
