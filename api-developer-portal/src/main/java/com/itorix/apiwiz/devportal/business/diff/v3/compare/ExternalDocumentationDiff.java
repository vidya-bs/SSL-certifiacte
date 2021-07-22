package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.ExternalDocumentation;

public class ExternalDocumentationDiff {

	public ExternalDocumentation diff(ExternalDocumentation oldDoc, ExternalDocumentation newDoc) {

		ExternalDocumentation changedExternalDocumentation = null;

		if (Objects.isNull(oldDoc) && Objects.isNull(newDoc)) {
			return null;
		}

		if (Objects.isNull(oldDoc) && Objects.nonNull(newDoc)) {
			changedExternalDocumentation = new ExternalDocumentation();
			changedExternalDocumentation.setDescription(newDoc.getDescription());
			changedExternalDocumentation.setUrl(newDoc.getUrl());
			changedExternalDocumentation.setExtensions(newDoc.getExtensions());
			return changedExternalDocumentation;
		}

		if (Objects.nonNull(oldDoc) && Objects.isNull(newDoc)) {
			return null;
		}

		if (ComparisonUtils.isDiff(oldDoc, newDoc)) {
			changedExternalDocumentation = new ExternalDocumentation();

			if (ComparisonUtils.isDiff(oldDoc.getDescription(), newDoc.getDescription())) {
				changedExternalDocumentation.setDescription(newDoc.getDescription());
			}

			if (ComparisonUtils.isDiff(oldDoc.getUrl(), newDoc.getUrl())) {
				changedExternalDocumentation.setUrl(newDoc.getUrl());
			}

			// TODO: extensions - later

			return changedExternalDocumentation;
		}

		return null;
	}
}
