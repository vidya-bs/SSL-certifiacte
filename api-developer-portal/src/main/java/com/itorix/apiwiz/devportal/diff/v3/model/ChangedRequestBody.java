package com.itorix.apiwiz.devportal.diff.v3.model;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.ContentDiff;

import io.swagger.v3.oas.models.parameters.RequestBody;

public class ChangedRequestBody extends RequestBody {

	private ContentDiff contentDiff;

	public ContentDiff getContentDiff() {
		return contentDiff;
	}

	public void setContentDiff(ContentDiff contentDiff) {
		this.contentDiff = contentDiff;
	}

}
