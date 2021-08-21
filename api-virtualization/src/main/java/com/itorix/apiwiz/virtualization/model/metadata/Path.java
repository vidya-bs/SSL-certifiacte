package com.itorix.apiwiz.virtualization.model.metadata;

public class Path {
	private String valueArray;

	private ValueCondition valueCondition;

	private String value;

	public String getValueArray() {
		return valueArray;
	}

	public void setValueArray(String valueArray) {
		this.valueArray = valueArray;
	}

	public ValueCondition getValueCondition() {
		return valueCondition;
	}

	public void setValueCondition(ValueCondition valueCondition) {
		this.valueCondition = valueCondition;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ClassPojo [valueArray = " + valueArray + ", valueCondition = " + valueCondition + ", value = " + value
				+ "]";
	}
}
