package com.itorix.apiwiz.performance.coverge.model;

public class Alert {
	private String unit;

	private int value;

	private String type;

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ClassPojo [unit = " + unit + ", value = " + value + ", type = " + type + "]";
	}
}
