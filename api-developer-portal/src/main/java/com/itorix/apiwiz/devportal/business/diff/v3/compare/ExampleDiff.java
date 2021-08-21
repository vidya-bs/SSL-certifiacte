package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.itorix.apiwiz.devportal.diff.v3.utils.ComparisonUtils;

import io.swagger.v3.oas.models.examples.Example;

public class ExampleDiff {

	private Map<String, Example> added = new LinkedHashMap<String, Example>();
	private Map<String, Example> missing = new LinkedHashMap<String, Example>();
	private Map<String, Example> changed = new LinkedHashMap<String, Example>();

	public ExampleDiff diff(Map<String, Example> left, Map<String, Example> right) {
		MapKeyDiff<String, Example> diffMap = MapKeyDiff.diff(left, right);

		if (Objects.nonNull(diffMap.getIncreased())) {
			this.added = diffMap.getIncreased();
		}

		if (Objects.nonNull(diffMap.getMissing())) {
			this.missing = diffMap.getMissing();
		}

		diffMap.getSharedKey().forEach(k -> {
			Example oldExample = left.get(k);
			Example newExample = right.get(k);

			if (ComparisonUtils.isDiff(oldExample, newExample)) {
				Example changedExample = new Example();

				if (ComparisonUtils.isDiff(oldExample.getSummary(), newExample.getSummary())) {
					changedExample.setSummary(newExample.getSummary());
				}

				if (ComparisonUtils.isDiff(oldExample.get$ref(), newExample.get$ref())) {
					changedExample.set$ref(newExample.get$ref());
				}

				if (ComparisonUtils.isDiff(oldExample.getDescription(), newExample.getDescription())) {
					changedExample.setDescription(newExample.getDescription());
				}

				if (ComparisonUtils.isDiff(oldExample.getExternalValue(), newExample.getExternalValue())) {
					changedExample.setExternalValue(newExample.getExternalValue());
				}

				if (ComparisonUtils.isDiff(oldExample.getValue(), newExample.getValue())) {
					changedExample.setValue(newExample.getValue());
				}

				// TODO: extensions - later

				this.changed.put(k, changedExample);
			}
		});

		return this;
	}

	public Map<String, Example> getAdded() {
		return added;
	}

	public void setAdded(Map<String, Example> added) {
		this.added = added;
	}

	public Map<String, Example> getMissing() {
		return missing;
	}

	public void setMissing(Map<String, Example> missing) {
		this.missing = missing;
	}

	public Map<String, Example> getChanged() {
		return changed;
	}

	public void setChanged(Map<String, Example> changed) {
		this.changed = changed;
	}

	public Boolean isNotEmpty() {
		return !this.getAdded().isEmpty() || !this.getChanged().isEmpty() || !this.getMissing().isEmpty();
	}
}
