package com.itorix.apiwiz.cicd.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SucessRatio {
	
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("sucessRatio")
	private int sucessRatio;
	
	@JsonProperty("total")
	private int total;
	
	@JsonProperty("success")
	private int success;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSucessRatio() {
		return sucessRatio;
	}

	public void setSucessRatio(int sucessRatio) {
		this.sucessRatio = sucessRatio;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "SucessRatio [name=" + name + ", sucessRatio=" + sucessRatio + ", total=" + total + ", success="
				+ success + "]";
	}
	
	
	
}
