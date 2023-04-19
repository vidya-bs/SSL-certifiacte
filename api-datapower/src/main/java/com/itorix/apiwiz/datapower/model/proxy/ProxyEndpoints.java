package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ProxyEndpoints implements Serializable {
	private String orgId;

	private String orgName;

	private String envName;

	@JsonProperty("isSaaS")
	private boolean isSaaS;

	private String proxyEndpoint;

	private String proxyUrl;
}
