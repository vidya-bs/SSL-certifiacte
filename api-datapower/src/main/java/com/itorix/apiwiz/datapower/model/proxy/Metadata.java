package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Metadata implements Serializable {
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
