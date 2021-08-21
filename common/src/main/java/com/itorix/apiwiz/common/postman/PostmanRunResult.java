package com.itorix.apiwiz.common.postman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostmanRunResult {
	private static final Logger logger = LoggerFactory.getLogger(PostmanRunResult.class);
	public int totalRequest = 0;
	public int failedRequest = 0;
	public int totalTest = 0;
	public int failedTest = 0;

	private Map result;

	public List<String> failedRequestName = new ArrayList<String>();
	public List<String> failedTestName = new ArrayList<String>();

	@Override
	public String toString() {
		String s = "Total Requests = " + totalRequest + "\n";
		s += "Failed Requests = " + failedRequest + "\n";
		s += "Total Tests = " + totalTest + "\n";
		s += "Failed Tests = " + failedTest + "\n";
		s += "Failed Request Names: " + failedRequestName + "\n";
		s += "Failed Test Names: " + failedTestName + "\n";
		logger.info(s);
		return s;
	}

	public boolean isSuccessful() {
		return failedRequest == 0 && failedTest == 0;
	}

	public Map getResult() {
		return result;
	}

	public void setResult(Map result) {
		this.result = result;
	}

	public void addResultItem(Object key, Object value) {
		if (this.result == null)
			this.result = new HashMap();
		this.result.put(key, value);
	}
}
