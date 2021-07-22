package com.itorix.apiwiz.identitymanagement.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequestContext implements Serializable, Cloneable {

	private static final long serialVersionUID = -8321298246752932991L;
	protected String hashcode;
	UserSession userSessionToken;
	protected RequestId requestId;
	protected Map<String, String> logMessage;

	public Map<String, String> getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(Map<String, String> logMessage) {
		this.logMessage = logMessage;
	}

	public String getHashcode() {
		if (getUserSessionToken() != null)
			return "" + getUserSessionToken().getId();
		else
			return "" + "?" + "-" + System.currentTimeMillis();
	}

	public void setHashcode(String code) {
		this.hashcode = code;
	}

	public UserSession getUserSessionToken() {
		return userSessionToken;
	}

	public void setUserSessionToken(UserSession userSessionToken) {
		this.userSessionToken = userSessionToken;
	}

	/**
	 * Show the value of the context.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return null;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public RequestId getRequestId() {
		return requestId;
	}

	public void setRequestId(RequestId requestId) {
		this.requestId = requestId;
	}
}
