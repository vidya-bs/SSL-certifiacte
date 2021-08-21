package com.itorix.apiwiz.devportal.diff.v3.model;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.ContentDiff;
import com.itorix.apiwiz.devportal.business.diff.v3.compare.ExampleDiff;

import io.swagger.v3.oas.models.headers.Header;

public class ChangedHeader extends Header {

	private ContentDiff contentDiff;

	private ExampleDiff exampleDiff;

	public ContentDiff getContentDiff() {
		return contentDiff;
	}

	public void setContentDiff(ContentDiff contentDiff) {
		this.contentDiff = contentDiff;
	}

	public ExampleDiff getExampleDiff() {
		return exampleDiff;
	}

	public void setExampleDiff(ExampleDiff exampleDiff) {
		this.exampleDiff = exampleDiff;
	}
}
