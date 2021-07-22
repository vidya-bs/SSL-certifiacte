package com.itorix.apiwiz.design.studio.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "Design.Swagger.Connectors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SwaggerIntegrations extends AbstractObject {

	private String swaggerName;
	private String swaggerId;
	private String oas;
	private String scm_folder;
	// @NotNull(message="scm_url cannot be missing or empty")
	private String scm_url;
	// @NotNull(message="scm_authorizationType cannot be missing or empty")
	private String scm_authorizationType;
	private String scm_branch;
	private String scm_repository;
	private String scm_username;
	private String scm_token;
	private String scm_type;
	private String scm_password;

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public String getSwaggerId() {
		return swaggerId;
	}

	public void setSwaggerId(String swaggerId) {
		this.swaggerId = swaggerId;
	}

	public String getOas() {
		return oas;
	}

	public void setOas(String oas) {
		this.oas = oas;
	}

	public String getScm_folder() {
		return scm_folder;
	}

	public void setScm_folder(String scm_folder) {
		this.scm_folder = scm_folder;
	}

	public String getScm_url() {
		return scm_url;
	}

	public void setScm_url(String scm_url) {
		this.scm_url = scm_url;
	}

	public String getScm_authorizationType() {
		return scm_authorizationType;
	}

	public void setScm_authorizationType(String scm_authorizationType) {
		this.scm_authorizationType = scm_authorizationType;
	}

	public String getScm_branch() {
		return scm_branch;
	}

	public void setScm_branch(String scm_branch) {
		this.scm_branch = scm_branch;
	}

	public String getScm_repository() {
		return scm_repository;
	}

	public void setScm_repository(String scm_repository) {
		this.scm_repository = scm_repository;
	}

	public String getScm_username() {
		return scm_username;
	}

	public void setScm_username(String scm_username) {
		this.scm_username = scm_username;
	}

	public String getScm_token() {
		return scm_token;
	}

	public void setScm_token(String scm_token) {
		this.scm_token = scm_token;
	}

	public String getScm_type() {
		return scm_type;
	}

	public void setScm_type(String scm_type) {
		this.scm_type = scm_type;
	}

	public String getScm_password() {
		return scm_password;
	}

	public void setScm_password(String scm_password) {
		this.scm_password = scm_password;
	}

	@Override
	public String toString() {
		return "ClassPojo [scm_folder = " + scm_folder + ", scm_url = " + scm_url + ", scm_authorizationType = "
				+ scm_authorizationType + ", scm_branch = " + scm_branch + ", scm_repository = " + scm_repository
				+ ", scm_username = " + scm_username + ", scm_token = " + scm_token + ", scm_type = " + scm_type
				+ ", scm_password = " + scm_password + "]";
	}

	@JsonIgnore
	public boolean isValid() throws ItorixException {
		List<String> missingFields = new ArrayList<>();
		if (this.scm_type == null || this.scm_type.trim() == "")
			missingFields.add("scm_type");
		if (this.scm_url == null || this.scm_url.trim() == "")
			missingFields.add("scm_url");
		if (this.scm_authorizationType == null || this.scm_authorizationType.trim() == "")
			missingFields.add("scm_authorizationType");
		else {
			if (this.scm_authorizationType.equals("PersonalToken") || this.scm_authorizationType.equals("OAuthToken"))
				if (this.scm_token == null || this.scm_token.trim() == "")
					missingFields.add("scm_token");
			if (this.scm_authorizationType.equals("Basic")) {
				if (this.scm_username == null || this.scm_username.trim() == "")
					missingFields.add("scm_username");
				if (this.scm_username == null || this.scm_username.trim() == "")
					missingFields.add("scm_password");
			}
		}
		if (this.scm_repository == null || this.scm_repository.trim() == "")
			missingFields.add("scm_repository");
		if (this.scm_branch == null || this.scm_branch.trim() == "")
			missingFields.add("scm_branch");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		return true;
	}

	private void raiseException(List<String> fileds) throws ItorixException {
		try {
			String message = new ObjectMapper().writeValueAsString(fileds);
			message = message.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", ");
			message = "Invalid request data! Missing mandatory data: " + message;
			throw new ItorixException(message, "General-1001");
		} catch (JsonProcessingException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("General-1001"), "General-1001");
		}
	}
}
