package com.itorix.apiwiz.devportal.business.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.itorix.apiwiz.devportal.model.ChangedEndpoint;
import com.itorix.apiwiz.devportal.model.ChangedOperation;
import com.itorix.apiwiz.devportal.model.Endpoint;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

public class SpecificationDiff {
	private String swaggerName;
	private String swaggerDescription;
	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;

	private SpecificationDiff() {
	}

	public static SpecificationDiff diff(Swagger oldSpec, Swagger newSpec) {
		SpecificationDiff instance = new SpecificationDiff();
		instance.swaggerName = newSpec.getInfo().getTitle();
		instance.swaggerDescription = newSpec.getInfo().getDescription();
		if (null == oldSpec || null == newSpec) {
			throw new IllegalArgumentException("cannot diff null spec.");
		}
		Map<String, Path> oldPaths = oldSpec.getPaths();
		Map<String, Path> newPaths = newSpec.getPaths();
		MapKeyDiff<String, Path> pathDiff = MapKeyDiff.diff(oldPaths, newPaths);
		instance.newEndpoints = convert2EndpointList(pathDiff.getIncreased());
		instance.missingEndpoints = convert2EndpointList(pathDiff.getMissing());
		instance.changedEndpoints = new ArrayList<ChangedEndpoint>();

		List<String> sharedKey = pathDiff.getSharedKey();
		ChangedEndpoint changedEndpoint = null;
		for (String pathUrl : sharedKey) {
			changedEndpoint = new ChangedEndpoint();
			changedEndpoint.setPathUrl(pathUrl);
			Path oldPath = oldPaths.get(pathUrl);
			Path newPath = newPaths.get(pathUrl);

			Map<HttpMethod, Operation> oldOperationMap = oldPath.getOperationMap();
			Map<HttpMethod, Operation> newOperationMap = newPath.getOperationMap();
			MapKeyDiff<HttpMethod, Operation> operationDiff = MapKeyDiff.diff(oldOperationMap, newOperationMap);
			Map<HttpMethod, Operation> increasedOperation = operationDiff.getIncreased();
			Map<HttpMethod, Operation> missingOperation = operationDiff.getMissing();
			changedEndpoint.setNewOperations(increasedOperation);
			changedEndpoint.setMissingOperations(missingOperation);

			List<HttpMethod> sharedMethods = operationDiff.getSharedKey();
			Map<HttpMethod, ChangedOperation> operas = new HashMap<HttpMethod, ChangedOperation>();
			ChangedOperation changedOperation = null;
			for (HttpMethod method : sharedMethods) {
				changedOperation = new ChangedOperation();
				Operation oldOperation = oldOperationMap.get(method);
				Operation newOperation = newOperationMap.get(method);
				changedOperation.setSummary(newOperation.getSummary());

				List<Parameter> oldParameters = oldOperation.getParameters();
				List<Parameter> newParameters = newOperation.getParameters();
				ParameterDiff parameterDiff = ParameterDiff
						.buildWithDefinition(oldSpec.getDefinitions(), newSpec.getDefinitions())
						.diff(oldParameters, newParameters);
				changedOperation.setAddParameters(parameterDiff.getIncreased());
				changedOperation.setMissingParameters(parameterDiff.getMissing());
				changedOperation.setChangedParameter(parameterDiff.getChanged());

				Property oldResponseProperty = getResponseProperty(oldOperation);
				Property newResponseProperty = getResponseProperty(newOperation);
				PropertyDiff propertyDiff = PropertyDiff.buildWithDefinition(oldSpec.getDefinitions(),
						newSpec.getDefinitions());
				propertyDiff.diff(oldResponseProperty, newResponseProperty);
				changedOperation.setAddProps(propertyDiff.getIncreased());
				changedOperation.setMissingProps(propertyDiff.getMissing());

				if (changedOperation.isDiff()) {
					operas.put(method, changedOperation);
				}
			}
			changedEndpoint.setChangedOperations(operas);

			instance.newEndpoints
					.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getNewOperations()));
			instance.missingEndpoints
					.addAll(convert2EndpointList(changedEndpoint.getPathUrl(), changedEndpoint.getMissingOperations()));

			if (changedEndpoint.isDiff()) {
				instance.changedEndpoints.add(changedEndpoint);
			}
		}

		return instance;
	}

	private static Property getResponseProperty(Operation operation) {
		Map<String, Response> responses = operation.getResponses();
		// temporary workaround for missing response messages
		if (responses == null)
			return null;
		Response response = responses.get("200");
		return null == response ? null : response.getSchema();
	}

	private static List<Endpoint> convert2EndpointList(Map<String, Path> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<String, Path> entry : map.entrySet()) {
			String url = entry.getKey();
			Path path = entry.getValue();

			Map<HttpMethod, Operation> operationMap = path.getOperationMap();
			for (Entry<HttpMethod, Operation> entryOper : operationMap.entrySet()) {
				HttpMethod httpMethod = entryOper.getKey();
				Operation operation = entryOper.getValue();

				Endpoint endpoint = new Endpoint();
				endpoint.setPathUrl(url);
				endpoint.setMethod(httpMethod);
				endpoint.setSummary(operation.getSummary());
				endpoint.setPath(path);
				endpoint.setOperation(operation);
				endpoints.add(endpoint);
			}
		}
		return endpoints;
	}

	private static Collection<? extends Endpoint> convert2EndpointList(String pathUrl, Map<HttpMethod, Operation> map) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (null == map)
			return endpoints;
		for (Entry<HttpMethod, Operation> entry : map.entrySet()) {
			HttpMethod httpMethod = entry.getKey();
			Operation operation = entry.getValue();
			Endpoint endpoint = new Endpoint();
			endpoint.setPathUrl(pathUrl);
			endpoint.setMethod(httpMethod);
			endpoint.setSummary(operation.getSummary());
			endpoint.setOperation(operation);
			endpoints.add(endpoint);
		}
		return endpoints;
	}

	public List<Endpoint> getNewEndpoints() {
		return newEndpoints;
	}

	public List<Endpoint> getMissingEndpoints() {
		return missingEndpoints;
	}

	public List<ChangedEndpoint> getChangedEndpoints() {
		return changedEndpoints;
	}

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public String getSwaggerDescription() {
		return swaggerDescription;
	}

	public void setSwaggerDescription(String swaggerDescription) {
		this.swaggerDescription = swaggerDescription;
	}
}
