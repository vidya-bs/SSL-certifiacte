package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedOpenAPI;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

public class SpecificationDiff {

	public static ChangedOpenAPI diff(OpenAPI oldSpec, OpenAPI newSpec) {

		if (null == oldSpec || null == newSpec) {
			throw new IllegalArgumentException("cannot diff null spec.");
		}

		ChangedOpenAPI changedOpenAPI = new ChangedOpenAPI();
		changedOpenAPI.setOpenapi(null);

		// OpeanAPI
		if (ComparisonUtils.isDiff(oldSpec.getOpenapi(), newSpec.getOpenapi())) {
			changedOpenAPI.setOpenapi(newSpec.getOpenapi());
		}

		// Info
		Info info = InfoDiff.diff(oldSpec.getInfo(), newSpec.getInfo());
		if (Objects.nonNull(info)) {
			changedOpenAPI.setInfo(info);
		}

		// ExternalDocs
		ExternalDocumentation changedExternalDocumentation = new ExternalDocumentationDiff()
				.diff(oldSpec.getExternalDocs(), newSpec.getExternalDocs());
		if (Objects.nonNull(changedExternalDocumentation)) {
			changedOpenAPI.setExternalDocs(changedExternalDocumentation);
		}

		// Servers
		ServerDiff serverDiff = new ServerDiff().diff(oldSpec.getServers(), newSpec.getServers());
		if (Objects.nonNull(serverDiff)) {
			changedOpenAPI.setServerDiff(serverDiff);
		}

		// Tags
		TagDiff tagDiff = new TagDiff().diff(oldSpec.getTags(), newSpec.getTags());
		if (Objects.nonNull(tagDiff)) {
			changedOpenAPI.setTagDiff(tagDiff);
		}

		// Paths
		PathsDiff pathsDiff = new PathsDiff().diff(oldSpec.getPaths(), newSpec.getPaths());
		if (Objects.nonNull(pathsDiff)) {
			changedOpenAPI.setPathsDiff(pathsDiff);
		}

		return changedOpenAPI;
	}
}
