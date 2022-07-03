package com.itorix.apiwiz.test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.postman.Point;
import com.itorix.apiwiz.common.model.postman.Properties;
import com.itorix.apiwiz.common.model.postman.Property;
import com.itorix.apiwiz.common.model.postman.Result;
import com.itorix.apiwiz.common.model.postman.Trace;

import net.sf.json.JSONArray;

public class Apigeex {
	public Map<String, Object> getTransactionData1(Trace tracesList, HashMap<String, HashMap<String, Long>> typeMap1)
			throws JsonParseException, JsonMappingException, IOException, ItorixException, ParseException,
			SecurityException {
		Trace trace = tracesList;
		HashMap<String, Object> typeMap = new HashMap<>();
		List<Object> averageTime = new ArrayList<Object>();
		for (int a = 0; a < 1; a++) {
			for (int i = 0; i < trace.getPoint().size(); i++) {
				Point p = trace.getPoint().get(i);
				List<Result> resultList1 = p.getResults();
				String id = p.getId();
				for (int j = 0; j < resultList1.size(); j++) {
					Result r = resultList1.get(j);
					Properties propertiesList = r.getProperties();
					if (propertiesList != null) {
						List<Property> propertyList = propertiesList.getProperty();
						for (int k = 0; k < propertyList.size(); k++) {
							Property p1 = propertyList.get(k);
							String name1 = p1.getName();
							String value = p1.getValue();
							if (id.equals("Execution")) {
								int previousIndex = i - 1;
								int beforePreviousIndex = i - 2;
								Point p2 = trace.getPoint().get(previousIndex);
								Point p3 = trace.getPoint().get(beforePreviousIndex);
								List<Result> resultList2 = p2.getResults();
								if (p2.getId().equals("Paused")) {
									resultList2 = p3.getResults();
								}
								Result previousResult = resultList2.get(j);
								String currentTime = r.getTimestamp();
								String previousTimestamp = previousResult.getTimestamp();
								SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
								Date date = format.parse(currentTime);
								long epoch = date.getTime();
								Date previousTSDate = format.parse(previousTimestamp);
								long previousTSEpoch = previousTSDate.getTime();
								long resultTime = epoch - previousTSEpoch;
								long max = resultTime;
								long min = resultTime;
								long count = 1;
								HashMap<String, Long> hash = new HashMap<>();
								Map<String, Object> policy = new HashMap<String, Object>();
								if (name1.equals("type")) {
									for (int x = 0; x < propertyList.size(); x++) {
										Property p4 = propertyList.get(x);
										String name2 = p4.getName();

										if (name2.equals("stepDefinition-name")) {
											if (typeMap1.containsKey(value)) {
												Object abc = typeMap.get(value);
												ObjectClass obj = new ObjectClass(resultTime, min, max, count);
												hash.put("averageTime",
														(typeMap1.get(value).get("averageTime") + resultTime) / 2);
												hash.put("count", (typeMap1.get(value).get("count") + 1));
												hash.put("max", typeMap1.get(value).get("max"));
												hash.put("min", typeMap1.get(value).get("min"));
												Long averageTime1 = (typeMap1.get(value).get("averageTime")
														+ resultTime) / 2;
												Long count1 = typeMap1.get(value).get("count") + 1;
												Long max1 = typeMap1.get(value).get("max");
												Long min1 = typeMap1.get(value).get("min");
												policy.put("averageTime", averageTime1);
												policy.put("stepType", value);
												policy.put("min", min1);
												policy.put("max", max1);
												policy.put("count", count1);
												if (typeMap1.get(value).get("max") < resultTime) {
													hash.put("max", resultTime);
													policy.put("max", resultTime);
												}
												if (typeMap1.get(value).get("min") > resultTime) {
													hash.put("min", resultTime);
													policy.put("min", resultTime);
												}
												typeMap1.put(value, hash);
												typeMap.put(value, policy);
											} else {
												ObjectClass obj = new ObjectClass(resultTime, min, max, count);
												obj.setAverageTime(resultTime);
												obj.setMin(min);
												obj.setMax(max);
												obj.setCount(count);
												policy.put("stepType", value);
												policy.put("min", obj.getMin());
												policy.put("averageTime", obj.getAverageTime());
												policy.put("max", obj.getMax());
												policy.put("count", obj.getCount());
												hash.put("averageTime", resultTime);
												hash.put("count", count);
												hash.put("max", max);
												hash.put("min", min);
												typeMap1.put(value, hash);
												typeMap.put(value, policy);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		System.out.println();
		for (Object key : typeMap.keySet()) {
			averageTime.add(typeMap.get(key));
		}
		Map<String, Object> policyTimes = new HashMap<String, Object>();
		policyTimes.put("step", averageTime);
		System.out.println(policyTimes);
		return policyTimes;
	}

	public Map<String, Object> getTransactionData2(Trace tracesList, HashMap<String, HashMap<String, Long>> nameMap1)
			throws JsonParseException, JsonMappingException, IOException, ItorixException, ParseException,
			SecurityException {
		Trace trace = tracesList;
		HashMap<String, Object> nameMap = new HashMap<>();
		List<Object> averageTime2 = new ArrayList<Object>();
		for (int a = 0; a < 1; a++) {
			for (int i = 0; i < trace.getPoint().size(); i++) {
				Point p = trace.getPoint().get(i);
				List<Result> resultList1 = p.getResults();
				String id = p.getId();
				for (int j = 0; j < resultList1.size(); j++) {
					Result r = resultList1.get(j);
					Properties propertiesList = r.getProperties();
					if (propertiesList != null) {
						List<Property> propertyList = propertiesList.getProperty();
						for (int k = 0; k < propertyList.size(); k++) {
							Property p1 = propertyList.get(k);
							String name1 = p1.getName();
							String value1 = p1.getValue();
							if (id.equals("Execution")) {
								int previousIndex = i - 1;
								int beforePreviousIndex = i - 2;
								Point p2 = trace.getPoint().get(previousIndex);
								Point p3 = trace.getPoint().get(beforePreviousIndex);
								List<Result> resultList2 = p2.getResults();
								if (p2.getId().equals("Paused")) {
									resultList2 = p3.getResults();
								}
								Result previousResult = resultList2.get(j);
								String currentTime = r.getTimestamp();
								String previousTimestamp = previousResult.getTimestamp();
								SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
								Date date = format.parse(currentTime);
								long epoch = date.getTime();
								Date previousTSDate = format.parse(previousTimestamp);
								long previousTSEpoch = previousTSDate.getTime();
								long resultTime = epoch - previousTSEpoch;
								long max = resultTime;
								long min = resultTime;
								long count = 1;
								String value2 = p1.getValue();
								for (int x = 0; x < propertyList.size(); x++) {
									Property p4 = propertyList.get(x);
									String name2 = p4.getName();

									if (name2.equals("type")) {
										value2 = p4.getValue();
									}

								}
								HashMap<String, Long> hash = new HashMap<>();
								Map<String, Object> policy = new HashMap<String, Object>();
								if (name1.equals("stepDefinition-name")) {
									for (int x = 0; x < propertyList.size(); x++) {
										Property p4 = propertyList.get(x);
										String name2 = p4.getName();

										if (name2.equals("stepDefinition-name")) {

											if (nameMap1.containsKey(value1)) {
												ObjectClass obj = new ObjectClass(resultTime, min, max, count);
												hash.put("averageTime",
														(nameMap1.get(value1).get("averageTime") + resultTime) / 2);
												hash.put("count", (nameMap1.get(value1).get("count") + 1));
												hash.put("max", nameMap1.get(value1).get("max"));
												hash.put("min", nameMap1.get(value1).get("min"));
												Long averageTime1 = (nameMap1.get(value1).get("averageTime")
														+ resultTime) / 2;
												Long count1 = (nameMap1.get(value1).get("count")) + 1;
												Long max1 = nameMap1.get(value1).get("max");
												Long min1 = nameMap1.get(value1).get("min");
												policy.put("averageTime", averageTime1);
												policy.put("stepType", value2);
												policy.put("min", min1);
												policy.put("max", max1);
												policy.put("count", count1);
												policy.put("stepName", value1);

												if (nameMap1.get(value1).get("max") < resultTime) {
													hash.put("max", resultTime);
													policy.put("max", resultTime);
												}
												if (nameMap1.get(value1).get("min") > resultTime) {
													hash.put("min", resultTime);
													policy.put("min", resultTime);
												}
												nameMap1.put(value1, hash);
												nameMap.put(value1, policy);
											} else {
												ObjectClass obj = new ObjectClass(resultTime, min, max, count);
												obj.setAverageTime(resultTime);
												obj.setMin(min);
												obj.setMax(max);
												obj.setCount(count);
												policy.put("stepType", value2);
												policy.put("min", obj.getMin());
												policy.put("averageTime", obj.getAverageTime());
												policy.put("max", obj.getMax());
												policy.put("count", obj.getCount());
												policy.put("stepName", value1);
												hash.put("averageTime", resultTime);
												hash.put("count", count);
												hash.put("max", max);
												hash.put("min", min);
												nameMap1.put(value1, hash);
												nameMap.put(value1, policy);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		for (Object key : nameMap.keySet()) {
			averageTime2.add(nameMap.get(key));
		}
		Map<String, Object> policyTimes2 = new HashMap<String, Object>();
		policyTimes2.put("policy", averageTime2);
		System.out.println(policyTimes2);
		return policyTimes2;
	}
}
