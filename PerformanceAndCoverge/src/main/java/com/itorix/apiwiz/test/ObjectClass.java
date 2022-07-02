package com.itorix.apiwiz.test;

import java.util.List;

public class ObjectClass {

	Long averageTime;
	Long min;
	Long max;
	Long count;
	String value;
	public ObjectClass(Long averageTime, Long min, Long max, Long count) {
		super();
		this.averageTime = averageTime;
		this.min = min;
		this.max = max;
		this.count = count;
	}

	public Long getAverageTime() {
		return averageTime;
	}

	public void setAverageTime(Long averageTime) {
		this.averageTime = averageTime;
	}

	public Long getMin() {
		return min;
	}

	public void setMin(Long min) {
		this.min = min;
	}

	public Long getMax() {
		return max;
	}

	public void setMax(Long max) {
		this.max = max;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
