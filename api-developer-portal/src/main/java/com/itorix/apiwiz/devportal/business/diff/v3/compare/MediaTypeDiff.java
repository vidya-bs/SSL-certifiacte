package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedMediaType;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

public class MediaTypeDiff {

	public ChangedMediaType diff(MediaType left, MediaType right) {
		ChangedMediaType changedMediaType = null;

		if (ComparisonUtils.isDiff(left, right)) {
			changedMediaType = new ChangedMediaType();

			if (ComparisonUtils.isNull(left.getExample(), right.getExample())) {
				changedMediaType.setExample(right.getExample());
			}

			if (ComparisonUtils.isDiff(left.getSchema(), right.getSchema())) {
				Schema<?> schemaDiff = new SchemaDiff().diff(left.getSchema(), right.getSchema());

				if (Objects.nonNull(schemaDiff)) {
					changedMediaType.setSchema(schemaDiff);
				}
			}

			ExampleDiff exampleDiff = new ExampleDiff().diff(left.getExamples(), right.getExamples());
			if (Objects.nonNull(exampleDiff)) {
				if (exampleDiff.isNotEmpty()) {
					changedMediaType.setExampleDiff(exampleDiff);
				}
			}

			EncodingDiff encodingDiff = new EncodingDiff().diff(left.getEncoding(), right.getEncoding());
			if (encodingDiff.isNotEmpty()) {
				changedMediaType.setExampleDiff(exampleDiff);
			}

			// TODO: extensions - later
		}

		return changedMediaType;
	}
}
