package com.itorix.mockserver.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.mockserver.common.model.MockLog;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MockLogger {

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private SpanAccessor spanAccessor;

	public void info(Map logMap) {
		StringBuffer logString = new StringBuffer();
		logString.append("date=" + logMap.get("date") + "||");
		logMap.remove("date");
		logString.append("guid=" + logMap.get("guid") + "||");
		logMap.remove("guid");
		logString.append("clientIp=" + logMap.get("clientIp") + "||");
		logMap.remove("clientIp");
		logString.append("path=" + logMap.get("path") + "||");
		logMap.remove("path");
		logString.append("logMessage=" + logMap.get("logMessage") + "||");
		logMap.remove("logMessage");
		log.info(logString.toString());
	}

	public Map getLogData(MockLog mockLog) {
		Map logMap = new HashMap();
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getDefault());
		logMap.put("date", df.format(date));
		logMap.put("guid", spanAccessor.getCurrentSpan().getTraceId());
		logMap.put("clientIp", mockLog.getClientIp());
		logMap.put("path", df.format(date));
		try {
			logMap.put("logMessage", objectMapper.writeValueAsString(mockLog));
		} catch (JsonProcessingException e) {
		}
		return logMap;
	}

}
