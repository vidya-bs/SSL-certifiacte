package com.itorix.apiwiz.cicd.dashboard.beans;

public class StageSuccessRatios {
	private String total;

	private String sucessRatio;

	private String name;

	private String success;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getSucessRatio() {
		return sucessRatio;
	}

	public void setSucessRatio(String sucessRatio) {
		this.sucessRatio = sucessRatio;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "ClassPojo [total = " + total + ", sucessRatio = " + sucessRatio + ", name = " + name + ", success = "
				+ success + "]";
	}
}
