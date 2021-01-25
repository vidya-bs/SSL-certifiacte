package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.itorix.apiwiz.devportal.diff.v3.model.ChangedOpenAPI;

import io.swagger.models.auth.AuthorizationValue;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class OpenAPIDiff {

	public static final String SWAGGER_VERSION_V3 = "3.0";


	private OpenAPI oldSpecification;
	private OpenAPI newSpecification;

	public static ChangedOpenAPI compare(String oldSpec, String newSpec) {
		return new OpenAPIDiff(oldSpec, newSpec, null, SWAGGER_VERSION_V3).compare();
	}
	
	public static ChangedOpenAPI compare(JsonNode oldSpec, JsonNode newSpec) {
		return new OpenAPIDiff(oldSpec, newSpec).compare();
	}
	
	private OpenAPIDiff(JsonNode oldSpec, JsonNode newSpec) {
		OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
		oldSpecification = openAPIV3Parser.readWithInfo(null, oldSpec).getOpenAPI();
		newSpecification = openAPIV3Parser.readWithInfo(null, oldSpec).getOpenAPI();
				
//		oldSpecification = openAPIV3Parser.read(oldSpec, true);
//		newSpecification = openAPIV3Parser.read(newSpec, true);

		if (null == oldSpec || null == newSpec) {
			throw new RuntimeException("cannot read api-doc from spec.");
		}
	}

	private OpenAPIDiff(String oldSpec, String newSpec, List<AuthorizationValue> auths, String version) {

		OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
		oldSpecification = openAPIV3Parser.read(oldSpec, null, null);
		newSpecification = openAPIV3Parser.read(newSpec, null, null);

		if (null == oldSpec || null == newSpec) {
			throw new RuntimeException("cannot read api-doc from spec.");
		}
	}

	private ChangedOpenAPI compare() {
		ChangedOpenAPI changedOpenAPI =  SpecificationDiff.diff(oldSpecification, newSpecification);
		changedOpenAPI.setSwaggerName(newSpecification.getInfo().getTitle());
		changedOpenAPI.setSwaggerDescription(newSpecification.getInfo().getDescription());
		changedOpenAPI.setOldVersion(oldSpecification.getInfo().getVersion());
		changedOpenAPI.setNewVersion(newSpecification.getInfo().getVersion());
		return changedOpenAPI;
	}

	public String getOldVersion() {
		return oldSpecification.getInfo().getVersion();
	}

	public String getNewVersion() {
		return newSpecification.getInfo().getVersion();
	}
}
