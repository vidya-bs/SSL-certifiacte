package com.itorix.apiwiz.common.model.integrations;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegrations;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Connectors.List")
public class Integration extends AbstractObject {

	private String type;
	private GitIntegration gitIntegration;
	private JfrogIntegration jfrogIntegration;
	private GoCDIntegration goCDIntegration;
	private WorkspaceIntegrations workspaceIntegration;
	private ApicIntegration apicIntegration;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public GitIntegration getGitIntegration() {
		return gitIntegration;
	}

	public void setGitIntegration(GitIntegration gitIntegration) {
		this.gitIntegration = gitIntegration;
	}

	public JfrogIntegration getJfrogIntegration() {
		return jfrogIntegration;
	}

	public void setJfrogIntegration(JfrogIntegration jfrogIntegration) {
		this.jfrogIntegration = jfrogIntegration;
	}

	public GoCDIntegration getGoCDIntegration() {
		return goCDIntegration;
	}

	public void setGoCDIntegration(GoCDIntegration goCDIntegration) {
		this.goCDIntegration = goCDIntegration;
	}

	public WorkspaceIntegrations getWorkspaceIntegration() {
		return workspaceIntegration;
	}

	public void setWorkspaceIntegration(WorkspaceIntegrations workspaceIntegration) {
		this.workspaceIntegration = workspaceIntegration;
	}

	public ApicIntegration getApicIntegration() {
		return apicIntegration;
	}

	public void setApicIntegration(ApicIntegration apicIntegration) {
		this.apicIntegration = apicIntegration;
	}
}
