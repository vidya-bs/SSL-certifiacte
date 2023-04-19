package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApigeeConfig implements Serializable {
	private List<String> apigeeVirtualHosts;

	private List<Environments> environments;

	private List<PolicyCategory> policyCategory;

	private List<Metadata> metadata;

	private List<TargetEndpoints> targetEndpoints;

	private List<ProxyEndpoints> proxyEndpoints;

	private ScmConfig scmConfig;

	private List<Pipelines> pipelines;

	private DesignArtifacts designArtifacts;
}
