package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Metadata {
	private int id;
	private String name;
	private String value;
	private String serviceName;
	private String proxyName;
	private String version;
	private String organization;
	private String teamOwner;
	private String ownerEmail;
	private String interfaceType;
}