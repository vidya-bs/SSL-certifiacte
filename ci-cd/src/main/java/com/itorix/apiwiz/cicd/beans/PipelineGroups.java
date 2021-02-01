package com.itorix.apiwiz.cicd.beans;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "metadata", "projectName", "notifications", "pipelines" })
public class PipelineGroups {

	public static final String LABEL_CREATED_TIME = "metadata.cts";
	
	@JsonProperty("metadata")
	private Metadata metadata;
	
	@Id
	@JsonProperty("projectName")
	private String projectName;
	
	@JsonProperty("defineName")
	private String defineName;
	
	@JsonProperty("notifications")
	private List<String> notifications = null;
	
	@JsonProperty("pipelines")
	private List<Pipeline> pipelines = null;
	
	@JsonProperty("metadata")
	public Metadata getMetadata() {
		return metadata;
	}

	@JsonProperty("metadata")
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	@JsonProperty("projectName")
	public String getProjectName() {
		return projectName;
	}

	@JsonProperty("projectName")
	public void setProjectName(String projectName) {
		if(this.defineName == null) this.defineName = projectName;
		this.projectName = projectName.replaceAll(" " , "-");
	}

	public String getDefineName() {
		return defineName;
	}

	@JsonProperty("notifications")
	public List<String> getNotifications() {
		return notifications;
	}

	@JsonProperty("notifications")
	public void setNotifications(List<String> notifications) {
		this.notifications = notifications;
	}

	@JsonProperty("pipelines")
	public List<Pipeline> getPipelines() {
		return pipelines;
	}

	@JsonProperty("pipelines")
	public void setPipelines(List<Pipeline> pipelines) {
		if(pipelines != null && pipelines.size() > 0 && pipelines.get(0).getDefineName() == null) {
			pipelines.get(0).setDefineName(defineName);
		}
		this.pipelines = pipelines;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PipelineGroups [metadata=");
		builder.append(metadata);
		builder.append(", projectName=");
		builder.append(projectName);
		builder.append(", notifications=");
		builder.append(notifications);
		builder.append(", pipelines=");
		builder.append(pipelines);
		builder.append("]");
		return builder.toString();
	}

}
