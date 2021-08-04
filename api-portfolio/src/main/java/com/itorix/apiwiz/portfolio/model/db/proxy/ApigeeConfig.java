package com.itorix.apiwiz.portfolio.model.db.proxy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApigeeConfig {
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
