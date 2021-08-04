package com.itorix.apiwiz.devportal.diff.v3.model;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.ExampleDiff;

import io.swagger.v3.oas.models.media.MediaType;

public class ChangedMediaType extends MediaType {

	private ExampleDiff exampleDiff;

	public ExampleDiff getExampleDiff() {
		return exampleDiff;
	}

	public void setExampleDiff(ExampleDiff exampleDiff) {
		this.exampleDiff = exampleDiff;
	}
}
