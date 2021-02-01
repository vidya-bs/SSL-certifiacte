package com.itorix.apiwiz.devportal.diff.v3.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.ApiResponsesDiff;
import com.itorix.apiwiz.devportal.business.diff.v3.compare.SecurityRequirementDiff;
import com.itorix.apiwiz.devportal.business.diff.v3.compare.ServerDiff;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

public class ChangedOperation extends Operation implements Changed {

	private String summary;

	private String description;

	private String operationId;

	private Boolean deprecated;

	private ServerDiff serverDiff;

	private ChangedRequestBody requestBody;

	private SecurityRequirementDiff securityRequirementDiff;

	private ApiResponsesDiff apiResponsesDiff;

	private Map<String, List<String>> tagDiff = new HashMap<>();

	private List<Parameter> addParameters = new ArrayList<Parameter>();
	private List<Parameter> missingParameters = new ArrayList<Parameter>();

	private List<ChangedParameter> changedParameter = new ArrayList<ChangedParameter>();

	private List<ElProperty> addProps = new ArrayList<ElProperty>();
	private List<ElProperty> missingProps = new ArrayList<ElProperty>();
	private List<ElProperty> changedProps = new ArrayList<ElProperty>();

	public Boolean getDeprecated() {
		return deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, List<String>> getTagDiff() {
		return tagDiff;
	}

	public void setTagDiff(Map<String, List<String>> tagDiff) {
		this.tagDiff = tagDiff;
	}

	public List<Parameter> getAddParameters() {
		return addParameters;
	}

	public void setAddParameters(List<Parameter> addParameters) {
		this.addParameters = addParameters;
	}

	public List<Parameter> getMissingParameters() {
		return missingParameters;
	}

	public void setMissingParameters(List<Parameter> missingParameters) {
		this.missingParameters = missingParameters;
	}

	public List<ChangedParameter> getChangedParameter() {
		return changedParameter;
	}

	public void setChangedParameter(List<ChangedParameter> list) {
		this.changedParameter = list;
	}

	public List<ElProperty> getAddProps() {
		return addProps;
	}

	public void setAddProps(List<ElProperty> addProps) {
		this.addProps = addProps;
	}

	public List<ElProperty> getMissingProps() {
		return missingProps;
	}

	public void setMissingProps(List<ElProperty> missingProps) {
		this.missingProps = missingProps;
	}

	public List<ElProperty> getChangedProps() {
		return changedProps;
	}

	public void setChangedProps(List<ElProperty> changedProps) {
		this.changedProps = changedProps;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public ServerDiff getServerDiff() {
		return serverDiff;
	}

	public void setServerDiff(ServerDiff serverDiff) {
		this.serverDiff = serverDiff;
	}

	public ChangedRequestBody getRequestBody() {
		return requestBody;
	}

	public ApiResponsesDiff getApiResponsesDiff() {
		return apiResponsesDiff;
	}

	public void setApiResponsesDiff(ApiResponsesDiff apiResponsesDiff) {
		this.apiResponsesDiff = apiResponsesDiff;
	}

	public void setRequestBody(ChangedRequestBody requestBody) {
		this.requestBody = requestBody;
	}

	public boolean isDiff() {
		return !tagDiff.isEmpty() || !Objects.isNull(deprecated) || !Objects.isNull(summary)
				|| !Objects.isNull(description) || !Objects.isNull(operationId) || !addParameters.isEmpty()
				|| !missingParameters.isEmpty() || !changedParameter.isEmpty() || Objects.nonNull(serverDiff) || Objects.nonNull(requestBody);
	}

	public boolean isDiffProp() {
		return !addProps.isEmpty() || !missingProps.isEmpty() || !changedProps.isEmpty();
	}

	public boolean isDiffParam() {
		return !addParameters.isEmpty() || !missingParameters.isEmpty() || !changedParameter.isEmpty();
	}

	public SecurityRequirementDiff getSecurityRequirementDiff() {
		return securityRequirementDiff;
	}

	public void setSecurityRequirementDiff(SecurityRequirementDiff securityRequirementDiff) {
		this.securityRequirementDiff = securityRequirementDiff;
	}

}
