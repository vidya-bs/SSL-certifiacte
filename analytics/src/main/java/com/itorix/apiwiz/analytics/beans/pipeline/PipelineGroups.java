package com.itorix.apiwiz.analytics.beans.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"projectName", "notifications", "pipelines"})
public class PipelineGroups {

	public static final String LABEL_CREATED_TIME = "metadata.cts";


	@Id
	@JsonProperty("projectName")
	private String projectName;

	@JsonProperty("defineName")
	private String defineName;

	@JsonProperty("notifications")
	private List<String> notifications = null;

	@JsonProperty("pipelines")
	private List<Pipeline> pipelines = null;

	@JsonProperty("projectName")
	public String getProjectName() {
		return projectName;
	}

	@JsonProperty("projectName")
	public void setProjectName(String projectName) {
		if (this.defineName == null)
			this.defineName = projectName;
		this.projectName = projectName.replaceAll(" ", "-");
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
		if (pipelines != null && pipelines.size() > 0 && pipelines.get(0).getDefineName() == null) {
			pipelines.get(0).setDefineName(defineName);
		}
		this.pipelines = pipelines;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PipelineGroups");
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
