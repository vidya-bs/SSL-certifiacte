package com.itorix.apiwiz.virtualization.model.logging;

import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Slf4j
@Document(collection = "Mock.Execution.Logs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockLog {
	@Id
	private String id;
	private String expectationId;
	private String expectationName;
	private String path;
	private String clientIp;
	private String loggedDate;
	private boolean wasMatched;
	private long loggedTime;
	private long traceId;
	private String httpRequest;
	private String httpResponse;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExpectationId() {
		return expectationId;
	}

	public void setExpectationId(String expectationId) {
		this.expectationId = expectationId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getLoggedDate() {
		return loggedDate;
	}

	public void setLoggedDate(String loggedDate) {
		this.loggedDate = loggedDate;
	}

	public boolean isWasMatched() {
		return wasMatched;
	}

	public void setWasMatched(boolean wasMatched) {
		this.wasMatched = wasMatched;
	}

	public long getLoggedTime() {
		return loggedTime;
	}

	public void setLoggedTime(long loggedTime) {
		this.loggedTime = loggedTime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getTraceId() {
		return traceId;
	}

	public void setTraceId(long traceId) {
		this.traceId = traceId;
	}

	/*
	 * public MockRequest getHttpRequest() { ObjectMapper objectMapper =
	 * ObjectMapperFactory.createObjectMapper(); try { return
	 * objectMapper.readValue(httpRequest, MockRequest.class); } catch
	 * (Exception e) { log.error("Exception occurred",e)(); return null; } }
	 */
	public JsonNode getHttpRequest() {
		ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
		try {
			return objectMapper.readTree(httpRequest);
		} catch (Exception e) {
			log.error("Exception occurred", e);
			return null;
		}
		// return httpRequest;
	}

	public void setHttpRequest(String httpRequest) {
		this.httpRequest = httpRequest;
	}

	/*
	 * public MockResponse getHttpResponse() { ObjectMapper objectMapper =
	 * ObjectMapperFactory.createObjectMapper(); try { MockResponse response =
	 * objectMapper.readValue(httpResponse, MockResponse.class); return
	 * response; } catch (Exception e) { log.error("Exception occurred",e)();
	 * return null; } }
	 */
	public JsonNode getHttpResponse() {
		ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
		try {
			return objectMapper.readTree(httpResponse);
		} catch (Exception e) {
			log.error("Exception occurred", e);
			return null;
		}
	}

	public void setHttpResponse(String httpResponse) {
		this.httpResponse = httpResponse;
	}

	public String getExpectationName() {
		return expectationName;
	}

	public void setExpectationName(String expectationName) {
		this.expectationName = expectationName;
	}
}
