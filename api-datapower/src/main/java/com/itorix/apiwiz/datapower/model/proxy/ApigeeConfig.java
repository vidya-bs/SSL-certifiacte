package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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
