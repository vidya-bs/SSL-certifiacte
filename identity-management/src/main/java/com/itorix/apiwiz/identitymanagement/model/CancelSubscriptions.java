package com.itorix.apiwiz.identitymanagement.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;

@Document(collection = "Users.Workspace.Cancel.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelSubscriptions {
	
	private String workspaceId;
	private String reasonCode;
	private String feedback;
	private String details;
	private String userId;
	private String userName;
	private String userEmail;
	private String status;
	
	public String getWorkspaceId() {
		return workspaceId;
	}
	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@JsonIgnore
	public boolean allowCancelSubscription() throws ItorixException{
		List<String> missingFields = new ArrayList<>();
		if(workspaceId != null && workspaceId.trim()=="")
			missingFields.add("workspaceId");
		if(reasonCode != null && reasonCode.trim()=="")
			missingFields.add("reasonCode");
		if(feedback != null && feedback.trim()=="")
			missingFields.add("feedback");
		if(missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}
	
	private void raiseException(List<String> fileds) throws ItorixException{
		try {
			String message = new ObjectMapper().writeValueAsString(fileds);
			message = message.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", ");
			message = "Invalid request data! Missing mandatory data: " + message ;
			throw new ItorixException(message,"USER_016");
		} catch (JsonProcessingException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("USER_016"),"USER_016");
		}
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
