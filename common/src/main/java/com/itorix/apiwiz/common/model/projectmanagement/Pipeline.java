package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.List;

public class Pipeline {

	private String branchType;
	private List<Stage> stages;

	public String getBranchType() {
		return branchType;
	}

	public void setBranchType(String branchType) {
		this.branchType = branchType;
	}

	public List<Stage> getStages() {
		return stages;
	}

	public void setStages(List<Stage> stages) {
		this.stages = stages;
	}

	@Override
	public String toString() {
		return "Pipeline [branchType=" + branchType + ", stages=" + stages + "]";
	}
}
