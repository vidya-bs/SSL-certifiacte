package com.itorix.apiwiz.cicd.beans;

public enum BackUpInterval {

	DAILY("daily"),WEEKLY("weekly"),MONTHLY("monthly");
	
	private String value;

	BackUpInterval(String value){
		this.value=value;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public static boolean isBackUpIntervalValid(String backUpIntervalType) {

		for (BackUpInterval backUpInterval : BackUpInterval.values()) {
			if (backUpInterval.value.equals(backUpIntervalType)) {
				return true;
			}
		}
		return false;

	}

}
