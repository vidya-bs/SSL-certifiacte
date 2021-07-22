package com.itorix.apiwiz.identitymanagement.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Component("ActivityLog")
@Document(collection = "Users.Audit.Logs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLog extends AbstractObject {

	public static final String LAST_CHANGED_AT = "last_Changed_At";
	public static final String USER_ID = "id_user";

	private String userId;

	private String operation;

	private String requestURI;

	private int statusCode;

	private long last_Changed_At;

	private String interactionId;

	private String id_user;

	@JsonIgnore
	public String getId_user() {
		return id_user;
	}

	@JsonIgnore
	public void setId_user(String id_user) {
		this.id_user = id_user;
	}

	public String getInteractionId() {
		return interactionId;
	}

	public void setInteractionId(String interactionId) {
		this.interactionId = interactionId;
	}

	public String getUser() {
		return userId;
	}

	@JsonIgnore
	public String getUserId() {
		return userId;
	}

	@JsonIgnore
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@JsonIgnore
	public long getLast_Changed_At() {
		return last_Changed_At;
	}

	@JsonIgnore
	public void setLast_Changed_At(long last_Changed_At) {
		this.last_Changed_At = last_Changed_At;
	}

	public String getTime() {
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
		Date date = new Date(this.last_Changed_At);
		return formatter.format(date);
	}

	/*
	 * public Date getLast_Changed_At() { return last_Changed_At; }
	 * 
	 * public void setLast_Changed_At(Date last_Changed_At) {
	 * this.last_Changed_At = last_Changed_At; }
	 */

	/*
	 * public Timestamp getLast_Changed_At() { return last_Changed_At; }
	 *
	 * public void setLast_Changed_At(Timestamp last_Changed_At) {
	 * this.last_Changed_At = last_Changed_At; }
	 */
	/*
	 * public String getLast_Changed_At() { return last_Changed_At; }
	 *
	 * public void setLast_Changed_At(String last_Changed_At) {
	 * this.last_Changed_At = last_Changed_At; }
	 */

}
