package com.itorix.apiwiz.testsuite.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.common.model.AbstractObject;

@Document(collection = "Test.Collections.Scheduler")
public class TestSuiteSchedule extends AbstractObject {

	private String testSuiteId;

	private String configId;

	private String testSuiteName;

	private String envName;

	private String recurrenceMode;

	private String scheduleTime;

	private List<Integer> days;

	private int executedDay;

	public String getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(String testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public String getRecurrenceMode() {
		return recurrenceMode;
	}

	public void setRecurrenceMode(String recurrenceMode) {
		this.recurrenceMode = recurrenceMode;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public List<Integer> getDays() {
		return days;
	}

	public void setDays(List<Integer> days) {
		this.days = days;
	}

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestSuiteSchedule [testSuiteId=");
		builder.append(testSuiteId);
		builder.append(", configId=");
		builder.append(configId);
		builder.append(", recurrenceMode=");
		builder.append(recurrenceMode);
		builder.append(", scheduleTime=");
		builder.append(scheduleTime);
		builder.append(", days=");
		builder.append(days);
		builder.append("]");
		return builder.toString();
	}

	public int getExecutedDay() {
		return executedDay;
	}

	public void setExecutedDay(int executedDay) {
		this.executedDay = executedDay;
	}
}
