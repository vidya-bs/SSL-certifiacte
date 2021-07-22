package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedSchema;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;

public class SchemaDiff {

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Schema diff(Schema leftSchema, Schema rightSchema) {

		ChangedSchema changedSchema = null;

		if (ComparisonUtils.isDiff(leftSchema, rightSchema)) {
			changedSchema = new ChangedSchema();

			if (ComparisonUtils.isDiff(leftSchema.getName(), rightSchema.getName())) {
				changedSchema.setName(rightSchema.getName());
			}

			if (ComparisonUtils.isDiff(leftSchema.getTitle(), rightSchema.getTitle())) {
				changedSchema.setTitle(rightSchema.getTitle());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMultipleOf(), rightSchema.getMultipleOf())) {
				changedSchema.setMultipleOf(rightSchema.getMultipleOf());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMaximum(), rightSchema.getMaximum())) {
				changedSchema.setMaximum(rightSchema.getMaximum());
			}

			if (ComparisonUtils.isDiff(leftSchema.getExclusiveMaximum(), rightSchema.getExclusiveMaximum())) {
				changedSchema.setExclusiveMaximum(rightSchema.getExclusiveMaximum());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMinimum(), rightSchema.getMinimum())) {
				changedSchema.setMinimum(rightSchema.getMinimum());
			}

			if (ComparisonUtils.isDiff(leftSchema.getExclusiveMinimum(), rightSchema.getExclusiveMinimum())) {
				changedSchema.setExclusiveMinimum(rightSchema.getExclusiveMinimum());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMaxLength(), rightSchema.getMaxLength())) {
				changedSchema.setMaxLength(rightSchema.getMaxLength());
			}

			if (ComparisonUtils.isDiff(leftSchema.getPattern(), rightSchema.getPattern())) {
				changedSchema.setPattern(rightSchema.getPattern());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMaxItems(), rightSchema.getMaxItems())) {
				changedSchema.setMaxItems(rightSchema.getMaxItems());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMinItems(), rightSchema.getMinItems())) {
				changedSchema.setMinItems(rightSchema.getMinItems());
			}

			if (ComparisonUtils.isDiff(leftSchema.getUniqueItems(), rightSchema.getUniqueItems())) {
				changedSchema.setUniqueItems(rightSchema.getUniqueItems());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMaxProperties(), rightSchema.getMaxProperties())) {
				changedSchema.setMaxProperties(rightSchema.getMaxProperties());
			}

			if (ComparisonUtils.isDiff(leftSchema.getMinProperties(), rightSchema.getMinProperties())) {
				changedSchema.setMinProperties(rightSchema.getMinProperties());
			}

			Map<String, List<String>> requiredDiff = ComparisonUtils.findDiff(leftSchema.getRequired(),
					rightSchema.getRequired());
			if (!requiredDiff.isEmpty()) {
				changedSchema.setRequiredDiff(requiredDiff);
			}

			if (ComparisonUtils.isDiff(leftSchema.getType(), rightSchema.getType())) {
				changedSchema.setType(rightSchema.getType());
			}

			// TODO: not - later

			// TODO: properties - later

			// TODO: additionalProperties - later

			if (ComparisonUtils.isDiff(leftSchema.getDescription(), rightSchema.getDescription())) {
				changedSchema.setDescription(rightSchema.getDescription());
			}

			if (ComparisonUtils.isDiff(leftSchema.getFormat(), rightSchema.getFormat())) {
				changedSchema.setFormat(rightSchema.getFormat());
			}

			if (ComparisonUtils.isDiff(leftSchema.get$ref(), rightSchema.get$ref())) {
				changedSchema.set$ref(rightSchema.get$ref());
			}

			if (ComparisonUtils.isDiff(leftSchema.getNullable(), rightSchema.getNullable())) {
				changedSchema.setNullable(rightSchema.getNullable());
			}

			if (ComparisonUtils.isDiff(leftSchema.getReadOnly(), rightSchema.getReadOnly())) {
				changedSchema.setReadOnly(rightSchema.getReadOnly());
			}

			if (ComparisonUtils.isDiff(leftSchema.getWriteOnly(), rightSchema.getWriteOnly())) {
				changedSchema.setWriteOnly(rightSchema.getWriteOnly());
			}

			// TODO: example - later

			ExternalDocumentation externalDocumentationDiff = new ExternalDocumentationDiff()
					.diff(leftSchema.getExternalDocs(), rightSchema.getExternalDocs());
			if (Objects.nonNull(externalDocumentationDiff)) {
				changedSchema.setExternalDocs(externalDocumentationDiff);
			}

			if (ComparisonUtils.isDiff(leftSchema.getDeprecated(), rightSchema.getDeprecated())) {
				changedSchema.setDeprecated(rightSchema.getDeprecated());
			}

			XML xmlDiff = new XMLDiff().diff(leftSchema.getXml(), rightSchema.getXml());
			if (Objects.nonNull(xmlDiff)) {
				changedSchema.setXml(xmlDiff);
			}

			// TODO: extensions - later

			// TODO: enum - later

			DiscriminatorDiff discriminatorDiff = new DiscriminatorDiff().diff(leftSchema.getDiscriminator(),
					rightSchema.getDiscriminator());
			if (Objects.nonNull(discriminatorDiff)) {
				changedSchema.setDiscriminatorDiff(discriminatorDiff);
			}
		}

		return changedSchema;
	}
}
