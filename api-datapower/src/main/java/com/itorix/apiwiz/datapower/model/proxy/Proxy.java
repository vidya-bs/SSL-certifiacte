package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.Getter;
import lombok.Setter;

@Component("proxy")
@Document(collection = "Datapower.Proxies.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Proxy extends AbstractObject implements Serializable {
	

	private String name;

	private String summary;

	private String proxyVersion;

	private List<String> basePaths;

	private boolean deprecate;

	private String gwProvider;

	private ApigeeConfig apigeeConfig;
	
	private String owner;

	@JsonProperty("ownerEmail")
	private String owner_email;

	private List<ScmHistory> history;
	
}
