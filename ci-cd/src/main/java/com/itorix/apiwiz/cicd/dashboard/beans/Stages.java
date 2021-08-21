package com.itorix.apiwiz.cicd.dashboard.beans;

public class Stages {
	private String result;

	private String duration;

	private String name;

	private String scheduled_date;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScheduled_date() {
		return scheduled_date;
	}

	public void setScheduled_date(String scheduled_date) {
		this.scheduled_date = scheduled_date;
	}

	@Override
	public String toString() {
		return "ClassPojo [result = " + result + ", duration = " + duration + ", name = " + name + ", scheduled_date = "
				+ scheduled_date + "]";
	}
}
