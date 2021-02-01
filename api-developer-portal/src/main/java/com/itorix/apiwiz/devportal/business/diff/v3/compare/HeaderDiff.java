package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedHeader;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;

public class HeaderDiff {

	Map<String, Header> added = new LinkedHashMap<String, Header>();
	Map<String, Header> missing = new LinkedHashMap<String, Header>();
	Map<String, Header> changed = new LinkedHashMap<String, Header>();

	public HeaderDiff diff(Map<String, Header> left, Map<String, Header> right) {

		MapKeyDiff<String, Header> diffMap = MapKeyDiff.diff(left, right);
		this.added = diffMap.getIncreased();
		this.missing = diffMap.getMissing();

		diffMap.getSharedKey().forEach(k -> {
			Header oldHeader = left.get(k);
			Header newHeader = right.get(k);

			if (ComparisonUtils.isDiff(oldHeader, newHeader)) {

				ChangedHeader changedHeader = new ChangedHeader();

				if (ComparisonUtils.isDiff(oldHeader.get$ref(), newHeader.get$ref())) {
					changedHeader.set$ref(newHeader.get$ref());
				}

				if (ComparisonUtils.isDiff(oldHeader.getDescription(), newHeader.getDescription())) {
					changedHeader.setDescription(newHeader.getDescription());
				}

				if (ComparisonUtils.isDiff(oldHeader.getDeprecated(), newHeader.getDeprecated())) {
					changedHeader.setDeprecated(newHeader.getDeprecated());
				}

				if (ComparisonUtils.isDiff(oldHeader.getExplode(), newHeader.getExplode())) {
					changedHeader.setDeprecated(newHeader.getExplode());
				}

				if (ComparisonUtils.isDiff(oldHeader.getRequired(), newHeader.getRequired())) {
					changedHeader.setRequired(newHeader.getRequired());
				}

				if (ComparisonUtils.isDiff(oldHeader.getStyle(), newHeader.getStyle())) {
					changedHeader.setStyle(newHeader.getStyle());
				}

				if (ComparisonUtils.isDiff(oldHeader.getExample(), newHeader.getExample())) {
					changedHeader.setExample(newHeader.getExample());
				}

				if (ComparisonUtils.isDiff(oldHeader.getContent(), newHeader.getContent())) {
					ContentDiff contentDiff = new ContentDiff().diff(oldHeader.getContent(), newHeader.getContent());
					if (contentDiff.isNotEmpty()) {
						changedHeader.setContentDiff(contentDiff);
					}
				}

				if (ComparisonUtils.isDiff(oldHeader.getExamples(), newHeader.getExamples())) {
					ExampleDiff exampleDiff = new ExampleDiff().diff(oldHeader.getExamples(), newHeader.getExamples());
					if (exampleDiff.isNotEmpty()) {
						changedHeader.setExampleDiff(exampleDiff);
					}
				}

				if (ComparisonUtils.isDiff(oldHeader.getSchema(), newHeader.getSchema())) {
					Schema<?> schemaDiff = new SchemaDiff().diff(oldHeader.getSchema(), oldHeader.getSchema());
					if (Objects.nonNull(schemaDiff)) {
						changedHeader.setSchema(schemaDiff);
					}
				}

				this.changed.put(k, changedHeader);
			}
		});

		return this;
	}
}
