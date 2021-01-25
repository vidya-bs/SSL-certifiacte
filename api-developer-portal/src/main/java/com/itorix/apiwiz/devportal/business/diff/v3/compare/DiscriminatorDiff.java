package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.Map;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.media.Discriminator;

public class DiscriminatorDiff {

	Map<String, String> added = new LinkedHashMap<>();
	Map<String, String> missing = new LinkedHashMap<>();
	Map<String, String> shared = new LinkedHashMap<>();
	Discriminator changedDiscriminator = null;

	public DiscriminatorDiff diff(Discriminator left, Discriminator right) {

		if (ComparisonUtils.isDiff(left, right)) {
			changedDiscriminator = new Discriminator();

			if (ComparisonUtils.isDiff(left.getPropertyName(), right.getPropertyName())) {
				changedDiscriminator.setPropertyName(right.getPropertyName());
			}

			MapKeyDiff<String, String> mappingDiff = MapKeyDiff.diff(left.getMapping(), right.getMapping());
			this.added = mappingDiff.getIncreased();
			this.missing = mappingDiff.getMissing();

			mappingDiff.getSharedKey().forEach(k -> {
				if (ComparisonUtils.isDiff(left.getMapping().get(k), right.getMapping().get(k))) {
					shared.put(k, right.getMapping().get(k));
				}
			});

		}

		return this;
	}

	public Map<String, String> getAdded() {
		return added;
	}

	public void setAdded(Map<String, String> added) {
		this.added = added;
	}

	public Map<String, String> getMissing() {
		return missing;
	}

	public void setMissing(Map<String, String> missing) {
		this.missing = missing;
	}

	public Map<String, String> getShared() {
		return shared;
	}

	public void setShared(Map<String, String> shared) {
		this.shared = shared;
	}

	public Discriminator getChangedDiscriminator() {
		return changedDiscriminator;
	}

	public void setChangedDiscriminator(Discriminator changedDiscriminator) {
		this.changedDiscriminator = changedDiscriminator;
	}

}
