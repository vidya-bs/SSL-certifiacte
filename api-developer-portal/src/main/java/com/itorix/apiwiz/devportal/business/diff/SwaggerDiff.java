package com.itorix.apiwiz.devportal.business.diff;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.itorix.apiwiz.devportal.business.diff.compare.SpecificationDiff;
import com.itorix.apiwiz.devportal.model.ChangedEndpoint;
import com.itorix.apiwiz.devportal.model.Endpoint;

import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerCompatConverter;
import io.swagger.parser.SwaggerParser;

public class SwaggerDiff {

	public static final String SWAGGER_VERSION_V2 = "2.0";

	private static Logger logger = LoggerFactory.getLogger(SwaggerDiff.class);

	private String swaggerName;
	private String swaggerDescription;
	private Swagger oldSpecSwagger;
	private Swagger newSpecSwagger;

	private List<Endpoint> newEndpoints;
	private List<Endpoint> missingEndpoints;
	private List<ChangedEndpoint> changedEndpoints;

	/**
	 * compare two swagger 1.x doc
	 *
	 * @param oldSpec
	 *            old api-doc location:Json or Http
	 * @param newSpec
	 *            new api-doc location:Json or Http
	 */
	public static SwaggerDiff compareV1(String oldSpec, String newSpec) {
		return compare(oldSpec, newSpec, null, null);
	}

	/**
	 * compare two swagger v2.0 doc
	 *
	 * @param oldSpec
	 *            old api-doc location:Json or Http
	 * @param newSpec
	 *            new api-doc location:Json or Http
	 */
	public static SwaggerDiff compareV2(String oldSpec, String newSpec) {
		return compare(oldSpec, newSpec, null, SWAGGER_VERSION_V2);
	}

	/**
	 * Compare two swagger v2.0
	 *
	 * @param oldSpec
	 *            old Swagger specification document in v2.0 format as a
	 *            JsonNode
	 * @param newSpec
	 *            new Swagger specification document in v2.0 format as a
	 *            JsonNode
	 */
	public static SwaggerDiff compareV2(JsonNode oldSpec, JsonNode newSpec) {
		return new SwaggerDiff(oldSpec, newSpec).compare();
	}

	public static SwaggerDiff compare(String oldSpec, String newSpec, List<AuthorizationValue> auths, String version) {
		return new SwaggerDiff(oldSpec, newSpec, auths, version).compare();
	}

	/**
	 * @param oldSpec
	 * @param newSpec
	 * @param auths
	 * @param version
	 */
	private SwaggerDiff(String oldSpec, String newSpec, List<AuthorizationValue> auths, String version) {
		if (SWAGGER_VERSION_V2.equals(version)) {
			SwaggerParser swaggerParser = new SwaggerParser();
			oldSpecSwagger = swaggerParser.read(oldSpec, auths, true);
			newSpecSwagger = swaggerParser.read(newSpec, auths, true);
		} else {
			SwaggerCompatConverter swaggerCompatConverter = new SwaggerCompatConverter();
			try {
				oldSpecSwagger = swaggerCompatConverter.read(oldSpec, auths);
				newSpecSwagger = swaggerCompatConverter.read(newSpec, auths);
			} catch (IOException e) {
				logger.error("cannot read api-doc from spec[version_v1.x]", e);
				return;
			}
		}
		if (null == oldSpecSwagger || null == newSpecSwagger) {
			throw new RuntimeException("cannot read api-doc from spec.");
		}
	}

	private SwaggerDiff(JsonNode oldSpec, JsonNode newSpec) {
		SwaggerParser swaggerParser = new SwaggerParser();
		oldSpecSwagger = swaggerParser.read(oldSpec, true);
		newSpecSwagger = swaggerParser.read(newSpec, true);
		if (null == oldSpecSwagger || null == newSpecSwagger) {
			throw new RuntimeException("cannot read api-doc from spec.");
		}
	}

	private SwaggerDiff compare() {
		SpecificationDiff diff = SpecificationDiff.diff(oldSpecSwagger, newSpecSwagger);
		this.newEndpoints = diff.getNewEndpoints();
		this.missingEndpoints = diff.getMissingEndpoints();
		this.changedEndpoints = diff.getChangedEndpoints();
		this.swaggerName = diff.getSwaggerName();
		this.swaggerDescription = diff.getSwaggerDescription();
		return this;
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

	public String getOldVersion() {
		return oldSpecSwagger.getInfo().getVersion();
	}

	public String getNewVersion() {
		return newSpecSwagger.getInfo().getVersion();
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
