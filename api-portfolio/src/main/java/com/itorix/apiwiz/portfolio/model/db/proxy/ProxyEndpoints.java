package com.itorix.apiwiz.portfolio.model.db.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ProxyEndpoints
{
	private String orgId;
	
    private String orgName;

    private String envName;

    @JsonProperty("isSaaS")
    private boolean isSaaS;

    private String proxyEndpoint;

    private String proxyUrl;

}



