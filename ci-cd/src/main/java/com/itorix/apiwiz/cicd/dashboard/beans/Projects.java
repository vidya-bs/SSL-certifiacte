package com.itorix.apiwiz.cicd.dashboard.beans;

public class Projects {
	private ProjectSuccessRatio projectSuccessRatio;

	private String name;

	private Pipelines[] pipelines;

	public ProjectSuccessRatio getProjectSuccessRatio() {
		return projectSuccessRatio;
	}

	public void setProjectSuccessRatio(ProjectSuccessRatio projectSuccessRatio) {
		this.projectSuccessRatio = projectSuccessRatio;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Pipelines[] getPipelines() {
		return pipelines;
	}

	public void setPipelines(Pipelines[] pipelines) {
		this.pipelines = pipelines;
	}

	@Override
	public String toString() {
		return "ClassPojo [projectSuccessRatio = " + projectSuccessRatio + ", name = " + name + ", pipelines = "
				+ pipelines + "]";
	}
}
