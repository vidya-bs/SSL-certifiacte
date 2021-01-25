package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedRequestBody;
import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.parameters.RequestBody;

public class RequestBodyDiff {

	public ChangedRequestBody diff(RequestBody left, RequestBody right) {
		
		if(Objects.isNull(left) && Objects.isNull(right)) {
			return null;
		}

		ChangedRequestBody changedReqBody = null;

		if (ComparisonUtils.isDiff(left, right)) {
			changedReqBody = new ChangedRequestBody();

			if (ComparisonUtils.isDiff(left.getDescription(), right.getDescription())) {
				changedReqBody.setDescription(right.getDescription());
			}

			if (ComparisonUtils.isDiff(left.get$ref(), right.get$ref())) {
				changedReqBody.set$ref(right.get$ref());
			}

			if (ComparisonUtils.isDiff(left.getRequired(), right.getRequired())) {
				changedReqBody.setRequired(right.getRequired());
			}

			ContentDiff contentDiff = new ContentDiff().diff(left.getContent(), right.getContent());
			if (contentDiff.isNotEmpty()) {
				changedReqBody.setContentDiff(contentDiff);
			}

			// TODO: extensions - later
		}

		return changedReqBody;
	}

}
