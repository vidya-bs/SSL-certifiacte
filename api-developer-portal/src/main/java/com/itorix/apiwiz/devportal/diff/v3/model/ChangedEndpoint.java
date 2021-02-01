package com.itorix.apiwiz.devportal.diff.v3.model;

import java.util.Map;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;

public class ChangedEndpoint implements Changed {

	private String pathUrl;

	private Map<HttpMethod, Operation> added;
	private Map<HttpMethod, Operation> missing;
	private Map<HttpMethod, ChangedOperation> changed;

	public Map<HttpMethod, Operation> getNewOperations() {
		return added;
	}

	public void setNewOperations(Map<HttpMethod, Operation> increasedOperation) {
		this.added = increasedOperation;
	}

	public Map<HttpMethod, Operation> getMissingOperations() {
		return missing;
	}

	public void setMissingOperations(Map<HttpMethod, Operation> missingOperations) {
		this.missing = missingOperations;
	}

	public Map<HttpMethod, ChangedOperation> getChangedOperations() {
		return changed;
	}

	public void setChangedOperations(Map<HttpMethod, ChangedOperation> changedOperations) {
		this.changed = changedOperations;
	}

	public String getPathUrl() {
		return pathUrl;
	}

	public void setPathUrl(String pathUrl) {
		this.pathUrl = pathUrl;
	}

	public boolean isDiff() {
//		newOperations.isEmpty() 
//		|| !missingOperations.isEmpty()
//		|| 
		return !changed.isEmpty();
	}

}
