package com.itorix.apiwiz.devportal.diff.v3.model;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.PathsDiff;
import com.itorix.apiwiz.devportal.business.diff.v3.compare.ServerDiff;
import com.itorix.apiwiz.devportal.business.diff.v3.compare.TagDiff;

import io.swagger.v3.oas.models.OpenAPI;

public class ChangedOpenAPI extends OpenAPI {

	private String swaggerName;
	private String swaggerDescription;
	private String oldVersion;
	private String newVersion;
	private PathsDiff pathsDiff;
	private ServerDiff serverDiff;
	private TagDiff tagDiff;

	public PathsDiff getPathsDiff() {
		return pathsDiff;
	}

	public void setPathsDiff(PathsDiff pathsDiff) {
		this.pathsDiff = pathsDiff;
	}

	public ServerDiff getServerDiff() {
		return serverDiff;
	}

	public void setServerDiff(ServerDiff serverDiff) {
		this.serverDiff = serverDiff;
	}

	public TagDiff getTagDiff() {
		return tagDiff;
	}

	public void setTagDiff(TagDiff tagDiff) {
		this.tagDiff = tagDiff;
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

	public String getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(String oldVersion) {
		this.oldVersion = oldVersion;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}
}
