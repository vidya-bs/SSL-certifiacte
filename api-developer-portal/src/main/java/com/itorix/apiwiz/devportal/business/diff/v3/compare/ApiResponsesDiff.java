package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedApiResponse;
import com.itorix.apiwiz.devportal.diff.v3.model.ChangedHeader;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ApiResponsesDiff {

	LinkedHashMap<String, ApiResponse> increased;
	LinkedHashMap<String, ApiResponse> missing;
	LinkedHashMap<String, ApiResponse> changed;

	public ApiResponsesDiff() {
		this.increased = new LinkedHashMap<String, ApiResponse>();
		this.missing = new LinkedHashMap<String, ApiResponse>();
		this.changed = new LinkedHashMap<String, ApiResponse>();
	}

	public ApiResponsesDiff diff(LinkedHashMap<String, ApiResponse> oldResponses,
			LinkedHashMap<String, ApiResponse> newResponses) {
		
		if (null == oldResponses || newResponses == null)
			return null;

		Map<String, Collection<String>> keyDiffMap = ComparisonUtils.findDiff(oldResponses.keySet(),
				newResponses.keySet());
		List<String> added = (List<String>) keyDiffMap.get("added");
		List<String> removed = (List<String>) keyDiffMap.get("removed");

		added.stream().forEach(k -> this.increased.put(k, newResponses.get(k)));
		removed.stream().forEach(k -> this.missing.put(k, oldResponses.get(k)));

		ChangedApiResponse changedAPIResponse = new ChangedApiResponse();

		oldResponses.forEach((k, v) -> {
			ApiResponse oldApiResponse = oldResponses.get(k);
			ApiResponse newApiResponse = newResponses.get(k);

			if (!(added.contains(k) || removed.contains(k))) {
				if (ComparisonUtils.isDiff(oldApiResponse.getDescription(), newApiResponse.getDescription())) {
					changedAPIResponse.setDescription(newApiResponse.getDescription());
				}

				if (ComparisonUtils.isDiff(oldApiResponse.get$ref(), newApiResponse.get$ref())) {
					changedAPIResponse.set$ref(newApiResponse.getDescription());
				}

				Map<String, Header> oldHeaders = oldApiResponse.getHeaders();
				Map<String, Header> newHeaders = newApiResponse.getHeaders();
				
				if(Objects.isNull(oldHeaders) && Objects.isNull(newHeaders)) {
					return;
				}

				if (ComparisonUtils.isDiff(oldHeaders, newHeaders)) {
					oldHeaders.forEach((key, oldHeader) -> {
						Header newHeader = newHeaders.get(key);
						ChangedHeader changedHeader = new ChangedHeader();

						if (ComparisonUtils.isDiff(oldHeader.getDescription(), newHeader.getDescription())) {
							changedHeader.setDescription(newHeader.getDescription());
						}

						if (ComparisonUtils.isDiff(oldHeader.get$ref(), newHeader.get$ref())) {
							changedHeader.set$ref(newHeader.get$ref());
						}

						if (ComparisonUtils.isDiff(oldHeader.getDeprecated(), newHeader.getDeprecated())) {
							changedHeader.setDeprecated(newHeader.getDeprecated());
						}

						if (ComparisonUtils.isDiff(oldHeader.getRequired(), newHeader.getRequired())) {
							changedHeader.setRequired(newHeader.getRequired());
						}

						if (ComparisonUtils.isDiff(oldHeader.getStyle(), newHeader.getStyle())) {
							changedHeader.setStyle(newHeader.getStyle());
						}

						if (ComparisonUtils.isDiff(oldHeader.getExplode(), newHeader.getExplode())) {
							changedHeader.setExplode(newHeader.getExplode());
						}

						Schema<?> schemaDiff = new SchemaDiff().diff(oldHeader.getSchema(), newHeader.getSchema());
						if (Objects.nonNull(schemaDiff)) {
							changedHeader.setSchema(schemaDiff);
						}

						ExampleDiff exampleDiff = new ExampleDiff().diff(oldHeader.getExamples(),
								newHeader.getExamples());
						if (Objects.nonNull(exampleDiff)) {
							changedHeader.setExampleDiff(exampleDiff);
						}

						if (ComparisonUtils.isDiff(oldHeader.getExample(),
								newHeader.getExample())) {
							changedHeader.setExample(newHeader.getExample());
						}

						ContentDiff contentDiff = new ContentDiff().diff(oldHeader.getContent(),
								newHeader.getContent());
						if (Objects.nonNull(contentDiff)) {
							changedHeader.setContentDiff(contentDiff);
						}

						// TODO: extensions - later
					});
				}
			}
			
		});

		return null;
	}
}
