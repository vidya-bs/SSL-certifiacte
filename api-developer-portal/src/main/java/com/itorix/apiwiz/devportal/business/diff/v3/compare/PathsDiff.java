package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedEndpoint;
import com.itorix.apiwiz.devportal.diff.v3.model.ChangedOperation;
import com.itorix.apiwiz.devportal.diff.v3.model.ChangedRequestBody;
import com.itorix.apiwiz.devportal.diff.v3.model.Endpoint;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PathsDiff {

	private List<Endpoint> added = new ArrayList<>();
	private List<Endpoint> missing = new ArrayList<>();
	private List<ChangedEndpoint> changed = new ArrayList<>();

	public PathsDiff diff(Paths oldPaths, Paths newPaths) {

		if (ComparisonUtils.isDiff(oldPaths, newPaths)) {

			MapKeyDiff<String, PathItem> pathDiffMap = MapKeyDiff.diff(oldPaths, newPaths);
			List<String> sharedKey = pathDiffMap.getSharedKey();

			sharedKey.stream().forEach((pathUrl) -> {
				ChangedEndpoint changedEndpoint = new ChangedEndpoint();
				changedEndpoint.setPathUrl(pathUrl);
				PathItem oldPath = oldPaths.get(pathUrl);
				PathItem newPath = newPaths.get(pathUrl);

				Map<HttpMethod, Operation> oldOperationMap = oldPath.readOperationsMap();
				Map<HttpMethod, Operation> newOperationMap = newPath.readOperationsMap();

				MapKeyDiff<HttpMethod, Operation> operationDiff = MapKeyDiff.diff(oldOperationMap, newOperationMap);

				Map<HttpMethod, Operation> addedOpearations = operationDiff.getIncreased();
				Map<HttpMethod, Operation> missingOpearations = operationDiff.getMissing();
				changedEndpoint.setNewOperations(addedOpearations);
				changedEndpoint.setMissingOperations(missingOpearations);

				List<HttpMethod> sharedMethods = operationDiff.getSharedKey();
				Map<HttpMethod, ChangedOperation> operationsMap = new HashMap<>();

				sharedMethods.stream().forEach((method) -> {
					ChangedOperation changedOperation = new ChangedOperation();
					Operation oldOperation = oldOperationMap.get(method);
					Operation newOperation = newOperationMap.get(method);

					// Tags comparison
					if (!oldOperation.getTags().equals(newOperation.getTags())) {
						Map<String, List<String>> tagDiff = findTagDiff(oldOperation.getTags(), newOperation.getTags());
						changedOperation.setTagDiff(tagDiff);
					}

					// Summary comparison
					String oldSusmmary = oldOperation.getSummary();
					String newSummary = newOperation.getSummary();

					if (ComparisonUtils.isDiff(oldSusmmary, newSummary)) {
						changedOperation.setSummary(newSummary == null ? "" : newSummary);
					}

					// Description comparison
					String oldDesc = oldOperation.getDescription();
					String newDesc = newOperation.getDescription();

					if (ComparisonUtils.isDiff(oldDesc, newDesc)) {
						changedOperation.setDescription(newDesc == null ? "" : newDesc);
					}

					ExternalDocumentation externalDocumentationDiff = new ExternalDocumentationDiff()
							.diff(oldOperation.getExternalDocs(), newOperation.getExternalDocs());
					if (Objects.nonNull(externalDocumentationDiff)) {
						changedOperation.setExternalDocs(externalDocumentationDiff);
					}

					String oldOpearationId = oldOperation.getOperationId();
					String newOperationId = newOperation.getOperationId();

					if (ComparisonUtils.isDiff(oldOpearationId, newOperationId)) {
						changedOperation.setOperationId(newOperationId == null ? "" : newOperationId);
					}

					// TODO: Parameters
					List<Parameter> oldParameters = oldOperation.getParameters();
					List<Parameter> newParameters = newOperation.getParameters();

					ParameterDiff paramDiff = new ParameterDiff().diff(oldParameters, newParameters);
					changedOperation.setAddParameters(paramDiff.getIncreased());
					changedOperation.setMissingParameters(paramDiff.getMissing());
					changedOperation.setChangedParameter(paramDiff.getChanged());

					ChangedRequestBody requestBodyDiff = new RequestBodyDiff().diff(oldOperation.getRequestBody(),
							newOperation.getRequestBody());
					if (Objects.nonNull(requestBodyDiff)) {
						changedOperation.setRequestBody(requestBodyDiff);
					}

					// TODO: API Responses - later
					ApiResponsesDiff resposeDiff = new ApiResponsesDiff().diff(oldOperation.getResponses(),
							newOperation.getResponses());
					if (Objects.nonNull(resposeDiff)) {
						changedOperation.setApiResponsesDiff(resposeDiff);
					}

					// TODO: callbacks - later

					changedOperation.setDeprecated(newOperation.getDeprecated());

					SecurityRequirementDiff securityRequirementDiff = new SecurityRequirementDiff()
							.diff(oldOperation.getSecurity(), newOperation.getSecurity());
					
					if(Objects.nonNull(securityRequirementDiff)) {
						if (securityRequirementDiff.isNotEmpty()) {
							changedOperation.setSecurityRequirementDiff(securityRequirementDiff);
						}
					}

					ServerDiff serverDiff = new ServerDiff().diff(oldOperation.getServers(), newOperation.getServers());
					if (Objects.nonNull(serverDiff)) {
						changedOperation.setServerDiff(serverDiff);
					}

					if (changedOperation.isDiff()) {
						operationsMap.put(method, changedOperation);
					}

					// TODO: extensions - later

				});

				changedEndpoint.setChangedOperations(operationsMap);

				if (changedEndpoint.isDiff()) {
					this.changed.add(changedEndpoint);
				}
			});
			
			pathDiffMap.getMissing().forEach((k,v) -> {
				this.missing.addAll(convert2EndpointList(k, v.readOperationsMap()));
			});
			
			pathDiffMap.getIncreased().forEach((k,v) -> {
				this.added.addAll(convert2EndpointList(k, v.readOperationsMap()));
			});
			
			return this;
		}

		return null;
	}

	public List<Endpoint> getAdded() {
		return added;
	}

	public void setAdded(List<Endpoint> added) {
		this.added = added;
	}

	public List<Endpoint> getMissing() {
		return missing;
	}

	public void setMissing(List<Endpoint> missing) {
		this.missing = missing;
	}

	public List<ChangedEndpoint> getChanged() {
		return changed;
	}

	public void setChanged(List<ChangedEndpoint> changed) {
		this.changed = changed;
	}

	@SuppressWarnings("unused")
	private static List<Endpoint> convert2EndpointList(Map<String, PathItem> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		map.forEach((url, path) -> {
			Map<HttpMethod, Operation> operationMap = path.readOperationsMap();
			operationMap.forEach((httpMethod, operation) -> {
				Endpoint endpoint = new Endpoint();
				endpoint.setPathUrl(url);
				endpoint.setMethod(httpMethod);
				endpoint.setSummary(operation.getSummary());
				endpoint.setPath(path);
				endpoint.setOperation(operation);
				endpoints.add(endpoint);
			});
		});

		return endpoints;
	}

	private static Collection<? extends Endpoint> convert2EndpointList(String pathUrl, Map<HttpMethod, Operation> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		map.forEach((httpMethod, operation) -> {
			Endpoint endpoint = new Endpoint();
			endpoint.setPathUrl(pathUrl);
			endpoint.setMethod(httpMethod);
			endpoint.setSummary(operation.getSummary());
			endpoint.setOperation(operation);
			endpoints.add(endpoint);
		});

		return endpoints;
	}

	private static Map<String, List<String>> findTagDiff(List<String> oldTags, List<String> newTags) {
		List<String> removed = oldTags.stream().filter(aObject -> {
			return !newTags.contains(aObject);
		}).collect(Collectors.toList());

		List<String> added = newTags.stream().filter(aObject -> !oldTags.contains(aObject))
				.collect(Collectors.toList());

		HashMap<String, List<String>> tagDiff = new HashMap<>();
		tagDiff.put("removed", removed);
		tagDiff.put("added", added);

		return tagDiff;
	}

}
