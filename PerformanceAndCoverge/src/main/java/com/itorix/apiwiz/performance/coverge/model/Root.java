package com.itorix.apiwiz.performance.coverge.model;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlAccessType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class Root {

	@XmlElement(name = "Debug")
	private ArrayList<Debug> debugList;

	public ArrayList<Debug> getDebugList() {
		return debugList;
	}

	public void setDebugList(ArrayList<Debug> debugList) {
		this.debugList = debugList;
	}

	public Root() {
		this.debugList = new ArrayList<Debug>();
	}

	public Map<String, Object> getAverageTimes() {
		Map<String, String> stepType = new HashMap<String, String>();
		Map<String, Object> steps = new HashMap<String, Object>();
		for (Debug debug : debugList) {
			List<StepName> stepLst = debug.getStepList();
			for (StepName step : stepLst) {
				String key = step.getValue();
				List<String> value;
				if (steps.get(key) != null)
					value = (List<String>) steps.get(key);
				else
					value = new ArrayList<String>();
				value.add(String.valueOf(step.getTimeTaken()));
				stepType.put(key, step.getStepType());
				steps.put(key, value);
			}
		}
		List<Object> averageTime = new ArrayList<Object>();
		for (Entry<String, Object> entry : steps.entrySet()) {
			String key = entry.getKey().toString();
			List<String> value = (List<String>) steps.get(key);

			double time = 0;
			double max = Double.parseDouble(value.get(0));
			double min = Double.parseDouble(value.get(0));
			for (Object elem : value) {
				time = time + Double.parseDouble((String) elem);
				if (Double.parseDouble((String) elem) > max)
					max = Double.parseDouble((String) elem);
				if (Double.parseDouble((String) elem) < min)
					min = Double.parseDouble((String) elem);
			}

			DecimalFormat df = new DecimalFormat("#.###");
			Map<String, Object> policy = new HashMap<String, Object>();
			policy.put("averageTime", Double.parseDouble(df.format(time / value.size())));
			policy.put("stepName", key);
			policy.put("count", Double.parseDouble(String.valueOf(value.size())));
			policy.put("min", Double.parseDouble(df.format(min)));
			policy.put("max", Double.parseDouble(df.format(max)));
			policy.put("stepType", stepType.get(key));

			averageTime.add(policy);
		}
		Map<String, Object> policyTimes = new HashMap<String, Object>();
		policyTimes.put("policy", averageTime);
		return policyTimes;
	}

	public Map<String, Object> getAveragePolicyTimes() {
		Map<String, Object> stepTypes = new HashMap<String, Object>();
		for (Debug debug : debugList) {
			List<StepName> stepLst = debug.getStepList();
			for (StepName step : stepLst) {
				String stepType = step.getStepType();
				double timeTaken = step.getTimeTaken();
				List<String> value;
				if (stepTypes.get(stepType) != null)
					value = (List<String>) stepTypes.get(stepType);
				else
					value = new ArrayList<String>();
				value.add(String.valueOf(timeTaken));
				stepTypes.put(stepType, value);
			}
		}

		List<Object> averageTime = new ArrayList<Object>();
		for (Entry<String, Object> step : stepTypes.entrySet()) {
			String key = step.getKey().toString();
			List<String> value = (List<String>) stepTypes.get(key);
			double time = 0;
			double max = Double.parseDouble(value.get(0));
			double min = Double.parseDouble(value.get(0));
			for (Object elem : value) {
				time = time + Double.parseDouble((String) elem);
				if (Double.parseDouble((String) elem) > max)
					max = Double.parseDouble((String) elem);
				if (Double.parseDouble((String) elem) < min)
					min = Double.parseDouble((String) elem);
			}

			DecimalFormat df = new DecimalFormat("#.###");
			Map<String, Object> policy = new HashMap<String, Object>();
			policy.put("averageTime", Double.parseDouble(df.format(time / value.size())));
			policy.put("stepType", key);
			policy.put("count", Double.parseDouble(String.valueOf(value.size())));
			policy.put("min", Double.parseDouble(df.format(min)));
			policy.put("max", Double.parseDouble(df.format(max)));
			averageTime.add(policy);
		}
		Map<String, Object> policyTimes = new HashMap<String, Object>();
		policyTimes.put("step", averageTime);
		return policyTimes;
	}
}
