package com.itorix.apiwiz.devportal.diff.v3.model;

import java.util.List;
import java.util.Map;

import com.itorix.apiwiz.devportal.business.diff.v3.compare.DiscriminatorDiff;

import io.swagger.v3.oas.models.media.Schema;

@SuppressWarnings("rawtypes")
public class ChangedSchema extends Schema {

	Map<String, List<String>> requiredDiff;

	DiscriminatorDiff discriminatorDiff;

	public Map<String, List<String>> getRequiredDiff() {
		return requiredDiff;
	}

	public void setRequiredDiff(Map<String, List<String>> requiredDiff) {
		this.requiredDiff = requiredDiff;
	}

	public DiscriminatorDiff getDiscriminatorDiff() {
		return discriminatorDiff;
	}

	public void setDiscriminatorDiff(DiscriminatorDiff discriminatorDiff) {
		this.discriminatorDiff = discriminatorDiff;
	}

}
