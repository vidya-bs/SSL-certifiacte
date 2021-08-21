package com.itorix.apiwiz.configmanagement.model.apigee;

public class ExpirySettings {
	private ExpiryDate expiryDate;
	private ExpiryDate timeoutInSec;
	private ExpiryDate timeOfDay;

	private String valuesNull;

	/** @return the timeoutInSec */
	public ExpiryDate getTimeoutInSec() {
		return timeoutInSec;
	}

	/**
	 * @param timeoutInSec
	 *            the timeoutInSec to set
	 */
	public void setTimeoutInSec(ExpiryDate timeoutInSec) {
		this.timeoutInSec = timeoutInSec;
	}

	/** @return the timeOfDay */
	public ExpiryDate getTimeOfDay() {
		return timeOfDay;
	}

	/**
	 * @param timeOfDay
	 *            the timeOfDay to set
	 */
	public void setTimeOfDay(ExpiryDate timeOfDay) {
		this.timeOfDay = timeOfDay;
	}

	public ExpiryDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(ExpiryDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getValuesNull() {
		return valuesNull;
	}

	public void setValuesNull(String valuesNull) {
		this.valuesNull = valuesNull;
	}
}
